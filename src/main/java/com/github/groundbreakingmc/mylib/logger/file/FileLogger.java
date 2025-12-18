package com.github.groundbreakingmc.mylib.logger.file;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;

public class FileLogger implements AutoCloseable {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static volatile ExecutorService globalExecutor;

    private final Path logPath;
    private final int maxSize;
    private final ExecutorService executor;

    private volatile boolean failed = false;
    private volatile boolean closed = false;

    // accessed only from executor thread
    private BufferedWriter writer;
    private long currentSize = 0;

    public FileLogger(String logFolder) throws IOException {
        this(logFolder, "latest.log", 2 << 20, globalExecutor());
    }

    public FileLogger(String logFolder, String logFileName) throws IOException {
        this(logFolder, logFileName, 2 << 20, globalExecutor());
    }

    public FileLogger(String logFolder, String logFileName, int maxSize) throws IOException {
        this(logFolder, logFileName, maxSize, globalExecutor());
    }

    public FileLogger(String logFolder, String logFileName, int maxSize, @Nullable ExecutorService executor) throws IOException {
        this.logPath = Path.of(logFolder, logFileName);
        this.maxSize = maxSize;
        this.executor = executor;

        Files.createDirectories(this.logPath.getParent());
        this.archiveIfExists();
        this.writer = Files.newBufferedWriter(this.logPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Asynchronous file logger with automatic log rotation and archiving.
     * <p>
     * All write operations are performed in a background thread via ExecutorService.
     * When a log file exceeds the size limit, it's automatically archived to a gzip file.
     */
    public void log(@NotNull Supplier<String> logEntry) {
        if (this.failed || this.closed) return;
        this.executor.execute(() -> {
            if (this.failed || this.closed) return;

            try {
                final String entry = time() + logEntry.get();
                final int entrySize = entry.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().length();

                if (this.currentSize + entrySize > this.maxSize) {
                    this.rotateLog();
                }

                this.writer.write(entry);
                this.writer.newLine();
                this.writer.flush();
                this.currentSize += entrySize;
            } catch (IOException e) {
                // Log write failed, silently ignore
                this.failed = true;
            }
        });
    }

    /**
     * Closes the logger and releases resources.
     * <p>
     * If a custom executor was provided, it will be shut down.
     * The global executor is not affected.
     */
    @Override
    public void close() {
        if (this.closed) return;
        this.closed = true;

        this.executor.execute(() -> {
            try {
                this.writer.close();
            } catch (IOException e) {
                // Ignore
            }
        });

        if (this.executor != globalExecutor) {
            this.executor.shutdown();
            try {
                this.executor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                this.executor.shutdownNow();
            }
        }
    }

    public static void shutdownGlobalExecutor() {
        synchronized (FileLogger.class) {
            if (globalExecutor != null) {
                globalExecutor.shutdown();
                try {
                    globalExecutor.awaitTermination(2, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    globalExecutor.shutdownNow();
                } finally {
                    globalExecutor = null;
                }
            }
        }
    }

    // ===== Utility Methods =====

    private static ExecutorService globalExecutor() {
        if (globalExecutor == null) {
            synchronized (FileLogger.class) {
                if (globalExecutor == null) {
                    globalExecutor = Executors.newSingleThreadExecutor(r -> {
                        final Thread thread = new Thread(r, "MyLib-FileLogger-Thread");
                        thread.setDaemon(true);
                        return thread;
                    });
                }
            }
        }
        return globalExecutor;
    }


    private static String time() {
        final Calendar cal = Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int day = cal.get(Calendar.DAY_OF_MONTH);
        final int hour = cal.get(Calendar.HOUR_OF_DAY);
        final int minute = cal.get(Calendar.MINUTE);
        final int second = cal.get(Calendar.SECOND);

        return new String(new char[]{
                '[',
                // yyyy
                (char) ('0' + year / 1000), (char) ('0' + (year / 100) % 10), (char) ('0' + (year / 10) % 10), (char) ('0' + year % 10),
                '-',
                (char) ('0' + month / 10), (char) ('0' + month % 10), // MM
                '-',
                (char) ('0' + day / 10), (char) ('0' + day % 10), // dd
                ' ',
                (char) ('0' + hour / 10), (char) ('0' + hour % 10), // hh
                ':',
                (char) ('0' + minute / 10), (char) ('0' + minute % 10), // mm
                ':',
                (char) ('0' + second / 10), (char) ('0' + second % 10), // ss
                ']',
                ' '
        });
    }

    private void archiveIfExists() throws IOException {
        if (!Files.exists(this.logPath) && Files.size(this.logPath) > 0) return;

        final String date = LocalDateTime.now().format(DATE_FORMATTER);
        int index = 1;
        Path archivePath;
        do {
            archivePath = this.logPath.getParent().resolve(date + "-" + index + ".log.gz");
            index++;
        } while (Files.exists(archivePath));

        try (final InputStream in = Files.newInputStream(this.logPath);
             final GZIPOutputStream gzip = new GZIPOutputStream(Files.newOutputStream(archivePath))) {
            in.transferTo(gzip);
        }

        Files.delete(this.logPath);
    }

    // called only from executor thread
    private void rotateLog() throws IOException {
        this.writer.close();
        this.archiveIfExists();
        this.writer = Files.newBufferedWriter(this.logPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        this.currentSize = 0;
    }
}
