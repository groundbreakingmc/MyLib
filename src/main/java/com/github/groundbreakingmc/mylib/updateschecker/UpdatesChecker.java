package com.github.groundbreakingmc.mylib.updateschecker;

import com.github.groundbreakingmc.mylib.logger.console.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Checks a UTF-8 text feed and stages plugin updates for installation on restart.
 * The first line is {@code version->jarUrl}; remaining nonblank lines are the changelog.
 * The URL is optional for notifications. Any different version string is an update.
 *
 * <p>Keep one instance for startup checks and update commands. Network operations use
 * the Paper/Folia asynchronous scheduler; completion callbacks may run asynchronously.
 */
public final class UpdatesChecker {

    private final Plugin plugin;
    private final Logger logger;
    private final String feedUrl;
    private final String updateCommand;

    private CompletableFuture<Status> operation;
    private boolean checking;
    private boolean autoUpdating;

    private volatile Status lastCheck = Status.NOT_CHECKED;

    /**
     * Last successful update link: null means no update, empty means the feed omitted its URL.
     */
    private volatile String downloadLink;

    /**
     * Creates a checker without starting a request.
     * The plugin supplies its version, logger and current JAR filename.
     *
     * @param plugin        the plugin to update
     * @param feedUrl       the HTTP or HTTPS URL of the text feed
     * @param updateCommand the command shown for manual updates; blank shows the JAR URL instead
     * @throws NullPointerException if any argument is null
     */
    public UpdatesChecker(@NotNull Plugin plugin, @NotNull String feedUrl, @NotNull String updateCommand) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = plugin.getLogger() instanceof Logger l ? l : Logger.create(plugin.getLogger());
        this.feedUrl = Objects.requireNonNull(feedUrl, "feedUrl");
        this.updateCommand = Objects.requireNonNull(updateCommand, "updateCommand");
    }

    /**
     * Checks the feed and prints the changelog and manual update command.
     * Concurrent checks in this mode share the same request and result.
     *
     * @return the check result, or {@link Status#BUSY} during an incompatible operation;
     * completes exceptionally if the check or scheduling fails
     */
    public CompletionStage<Status> check() {
        return this.startOperation(true, false);
    }

    /**
     * Checks the feed and optionally downloads the found update in the same task.
     * Concurrent checks with the same mode share their result; different modes return BUSY.
     * A failed download preserves the successful check and its cached link.
     *
     * @param autoUpdate whether to download a found update and omit the manual command hint
     * @return the check result, DOWNLOADED after automatic downloading, or BUSY;
     * completes exceptionally if checking, downloading or scheduling fails
     */
    public CompletionStage<Status> check(boolean autoUpdate) {
        return this.startOperation(true, autoUpdate);
    }

    /**
     * Returns the latest completed check, including a failed attempt.
     * Starting a check does not immediately change this value. Download failures do not change it.
     *
     * @return NOT_CHECKED initially, then NO_UPDATE, UPDATE_AVAILABLE or FAILED
     */
    public Status lastCheck() {
        return this.lastCheck;
    }

    /**
     * Reads the result of the last successful check without making a request.
     * A later failed check preserves this result. Downloading does not clear it.
     *
     * @return whether a differing version was found, even if its feed omitted a download URL
     */
    public boolean hasUpdate() {
        return this.downloadLink != null;
    }

    /**
     * Downloads the cached update without requesting the feed again.
     * A temporary file replaces the staged JAR only after a successful transfer;
     * its name matches the current plugin JAR. Installation happens on server restart.
     *
     * @return DOWNLOADED, BUSY, or NOT_CHECKED/NO_UPDATE when no update is cached;
     * completes exceptionally on failure, including an unsuccessful prior check with no cache
     */
    public CompletionStage<Status> downloadUpdate() {
        return this.startOperation(false, false);
    }

    private synchronized CompletionStage<Status> startOperation(boolean check, boolean autoUpdate) {
        if (this.operation != null && !this.operation.isDone()) {
            if (check && this.checking && autoUpdate == this.autoUpdating) {
                return this.operation.minimalCompletionStage();
            }
            return CompletableFuture.completedStage(Status.BUSY);
        }
        if (!check && this.downloadLink == null) {
            if (this.lastCheck == Status.FAILED) {
                return CompletableFuture.failedStage(new IllegalStateException("The last update check failed. Check again first."));
            }
            return CompletableFuture.completedStage(this.lastCheck);
        }

        final CompletableFuture<Status> future = new CompletableFuture<>();
        this.operation = future;
        this.checking = check;
        this.autoUpdating = autoUpdate;
        try {
            Bukkit.getAsyncScheduler().runNow(this.plugin,
                    task -> this.runOperation(check, autoUpdate, future));
        } catch (final RuntimeException ex) {
            if (check) this.lastCheck = Status.FAILED;
            this.fail(future, ex, "schedule");
        }
        return future.minimalCompletionStage();
    }

    private void runOperation(boolean check, boolean autoUpdate, CompletableFuture<Status> future) {
        boolean checked = false;
        try {
            Status status = Status.UPDATE_AVAILABLE;
            if (check) {
                status = this.checkNow(!autoUpdate);
                this.lastCheck = status;
                checked = true;
            }
            if (status == Status.UPDATE_AVAILABLE && (!check || autoUpdate)) {
                this.downloadJar(this.downloadLink);
                this.logger.info("Update downloaded successfully. Restart the server to install it.");
                status = Status.DOWNLOADED;
            }
            future.complete(status);
        } catch (final Exception ex) {
            if (check && !checked) this.lastCheck = Status.FAILED;
            this.fail(future, ex, check && !checked ? "check for" : "download");
        }
    }

    private void fail(CompletableFuture<Status> future, Exception error, String action) {
        future.completeExceptionally(error);
        this.logger.warn("Failed to " + action + " update: " + error.getMessage());
    }

    private Status checkNow(boolean showCommand) throws IOException {
        final HttpURLConnection connection = this.openConnection(this.feedUrl, 10000);
        try {
            final String availableVersion;
            final String link;
            final List<String> changes;
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                final String header = reader.readLine();
                if (header == null || header.isBlank()) throw new IOException("Update feed is empty");
                final String[] versionInfo = header.split("->", 2);
                availableVersion = versionInfo[0].trim();
                if (availableVersion.isEmpty()) throw new IOException("Update feed has no version");
                link = versionInfo.length > 1 ? versionInfo[1].trim() : "";
                changes = reader.lines().filter(line -> !line.isBlank()).toList();
            }

            final String currentVersion = this.plugin.getPluginMeta().getVersion();
            if (currentVersion.equals(availableVersion)) {
                this.downloadLink = null;
                return Status.NO_UPDATE;
            }

            this.logger.info("[UPDATE] ╓");
            this.logger.info("[UPDATE] ╠ Installed version: " + currentVersion);
            this.logger.info("[UPDATE] ╠ Available version: " + availableVersion);
            this.logger.info("[UPDATE] ╚╗");
            for (final String change : changes) this.logger.info("[UPDATE]  ╠ " + change);
            if (showCommand) {
                this.logger.info("[UPDATE] ╔╝");
                if (!this.updateCommand.isBlank()) {
                    this.logger.info("[UPDATE] ╠ Use '" + this.updateCommand + "' to download");
                } else if (!link.isEmpty()) {
                    this.logger.info("[UPDATE] ╠ Download: " + link);
                }
                this.logger.info("[UPDATE] ╙");
            } else {
                this.logger.info("[UPDATE] ─╜");
            }
            this.downloadLink = link;
            return Status.UPDATE_AVAILABLE;
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection openConnection(String address, int readTimeout) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) URI.create(address).toURL().openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(readTimeout);
        try {
            final int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + responseCode + " from " + address);
            }
            return connection;
        } catch (final IOException | RuntimeException ex) {
            connection.disconnect();
            throw ex;
        }
    }

    private void downloadJar(String downloadLink) throws IOException {
        if (downloadLink.isEmpty()) throw new IOException("Update feed has no download link");
        final Path pluginJar;
        try {
            pluginJar = Path.of(this.plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (final URISyntaxException ex) {
            throw new IOException("Cannot resolve the plugin JAR path", ex);
        }
        final Path updateFolder = Bukkit.getUpdateFolderFile().toPath();
        Files.createDirectories(updateFolder);
        final Path destination = updateFolder.resolve(pluginJar.getFileName());
        final Path temporary = Files.createTempFile(updateFolder, "plugin-update-", ".tmp");
        HttpURLConnection connection = null;
        try {
            connection = this.openConnection(downloadLink, 120000);
            try (final InputStream input = connection.getInputStream()) {
                Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            if (connection != null) connection.disconnect();
            Files.deleteIfExists(temporary);
        }
    }

    /**
     * An operation outcome or the state of the latest check.
     * Operational errors complete their stage exceptionally; FAILED is used by lastCheck().
     */
    public enum Status {
        /**
         * No check has completed yet.
         */
        NOT_CHECKED,
        /**
         * The last successful check found the running version.
         */
        NO_UPDATE,
        /**
         * A different version is available.
         */
        UPDATE_AVAILABLE,
        /**
         * The update is staged for installation on restart.
         */
        DOWNLOADED,
        /**
         * An incompatible operation is already running.
         */
        BUSY,
        /**
         * The last check failed; its previous successful cache is preserved.
         */
        FAILED
    }
}
