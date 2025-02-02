package com.github.groundbreakingmc.mylib.updateschecker;

import com.github.groundbreakingmc.mylib.logger.Logger;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public final class UpdatesChecker {

    private final Plugin plugin;
    private final Logger logger;
    private final String stringURL;
    private final String errorMessage;
    private final String updateCommand;

    @Getter
    @Accessors(fluent = true)
    private static boolean hasUpdate = false;
    private static String downloadLink = null;

    public UpdatesChecker(final Plugin plugin, final Logger logger, final String url, final String issueURL, final String updateCommand) {
        this.plugin = plugin;
        this.logger = logger;
        this.stringURL = url;
        this.errorMessage = "Please create an issue " + issueURL + " and report this error.";
        this.updateCommand = updateCommand;
    }

    public void check(final boolean downloadUpdate, final boolean commandCall) {
        try {
            final URL url = new URL(this.stringURL);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    final String[] body = reader.readLine().split("\n", 2);
                    final String[] versionInfo = body[0].split("->");

                    if (hasUpdate = this.isHigher(versionInfo[0])) {
                        if (!commandCall) {
                            this.logUpdate(body[1].split("\n"), versionInfo[0], downloadUpdate);
                        }

                        if (downloadUpdate && (downloadLink = versionInfo[1]) != null) {
                            this.downloadJar(false);
                        }

                        return;
                    }

                    this.logger.info("No updates were found!");
                }
            } else {
                this.logger.warn("Check was canceled with response code: " + responseCode + ".");
                this.logger.warn(this.errorMessage);
            }
        } catch (final IOException ex) {
            this.logger.warn("Failed to check for updates: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean isHigher(final String newVersion) {
        final String pluginVersion = this.plugin.getDescription().getVersion();
        final int currentVersionNum = Integer.parseInt(pluginVersion.replace(".", ""));
        final int newVersionNum = Integer.parseInt(newVersion.replace(".", ""));
        return currentVersionNum < newVersionNum;
    }

    public void downloadJar(final boolean commandCall) {
        if (downloadLink == null) {
            this.check(true, commandCall);
            return;
        } else if (downloadLink.isEmpty()) {
            this.logger.warn("Download link for new version of the plugin is empty!.");
            this.logger.warn(this.errorMessage);
            return;
        }

        try {
            final URL url = new URL(downloadLink);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(120000);

            final int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                final File updateFolder = Bukkit.getUpdateFolderFile();
                final String jarFileName = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
                final File outputFile = new File(updateFolder, jarFileName);

                final long totalSize = connection.getContentLengthLong();
                try (final InputStream inputStream = connection.getInputStream();
                        final FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {

                    final byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    int downloaded = 0;

                    while ((bytesRead = inputStream.read(dataBuffer)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                        downloaded += bytesRead;

                        if (totalSize > 0) {
                            int progress = (int) ((downloaded / (double) totalSize) * 100);
                            if (progress % 10 == 0) {
                                this.logger.info("Downloaded: " + (downloaded / 1024) + " / " + (totalSize / 1024) + " KB (" + progress + "%)");
                            }
                        }
                    }

                    this.logger.info("Update downloaded successfully.");
                }
            } else {
                this.logger.warn("Jar downloading was canceled with response code: " + responseCode + ".");
                this.logger.warn(this.errorMessage);
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    private void logUpdate(final String[] body, final String newVersion, final boolean isAutoUpdateEnabled) {
        this.logger.info("[UPDATE] ╓");
        this.logger.info("[UPDATE] ╠ New version found - v" + newVersion);
        this.logger.info("[UPDATE] ╚╗");

        for (final String info : body) {
            this.logger.info("[UPDATE]  ╠ " + info);
        }

        if (!isAutoUpdateEnabled) {
            this.logger.info("[UPDATE] ╔╝");
            this.logger.info("[UPDATE] ╠ Use '" + this.updateCommand + "' to download");
            this.logger.info("[UPDATE] ╙");
        } else {
            this.logger.info("[UPDATE] ─╜");
        }
    }
}
