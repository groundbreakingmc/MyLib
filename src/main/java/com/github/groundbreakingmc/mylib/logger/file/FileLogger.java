package com.github.groundbreakingmc.mylib.logger.file;

import lombok.Setter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings("unused")
public class FileLogger implements AutoCloseable {

    private static volatile ExecutorService GENERAL_EXECUTOR;

    private final BufferedWriter writer;
    @Setter
    private ExecutorService executor;

    public FileLogger(final String logFolder) throws IOException {
        this(logFolder, "latest.log");
    }

    public FileLogger(final String logFolder, final String logFileName) throws IOException {
        final File logFile = new File(logFolder, logFileName);

        if (logFile.exists() && Files.size(logFile.toPath()) > 0) {
            archiveLogFile(logFile, logFolder);
        } else {
            try {
                Files.createDirectories(Paths.get(logFolder));
                if (!logFile.exists()) {
                    Files.createFile(logFile.toPath());
                }
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        this.writer = Files.newBufferedWriter(logFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        this.executor = getOrCreateExecutor();
    }

    private static synchronized ExecutorService getOrCreateExecutor() {
        if (GENERAL_EXECUTOR == null) {
            GENERAL_EXECUTOR = Executors.newSingleThreadExecutor(threadFactory -> {
                final Thread thread = new Thread(threadFactory, "MyLib-File-Logger");
                thread.setDaemon(true);
                return thread;
            });
        }
        return GENERAL_EXECUTOR;
    }

    public static synchronized void shutdownGlobalExecutor() {
        if (GENERAL_EXECUTOR != null && !GENERAL_EXECUTOR.isShutdown()) {
            GENERAL_EXECUTOR.shutdown();
            GENERAL_EXECUTOR = null;
        }
    }

    public void log(final Supplier<String> logEntry) {
        executor.execute(() -> {
            try {
                writer.write(getTime() + logEntry.get());
                writer.newLine();
                writer.flush();
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private static void archiveLogFile(final File logFile, final String logFolder) {
        getOrCreateExecutor().execute(() -> {
            try {
                final File archive = getArchiveFile(logFolder);

                try (final FileInputStream inputStream = new FileInputStream(logFile);
                     final FileOutputStream outputStream = new FileOutputStream(archive);
                     final GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
                    final byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        gzip.write(buffer, 0, length);
                    }
                }

                Files.delete(logFile.toPath());
                Files.createFile(logFile.toPath());
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private static File getArchiveFile(final String logFolder) {
        final String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        int index = 1;
        File archive;

        do {
            archive = new File(logFolder, date + "-" + index + ".log.gz");
            index++;
        } while (archive.exists());

        return archive;
    }

    private static String getTime() {
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

    public void useDefaultExecutor() {
        this.executor = getOrCreateExecutor();
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
        if (this.executor != GENERAL_EXECUTOR && !this.executor.isShutdown()) {
            this.executor.shutdown();
        }
    }

    @Deprecated(forRemoval = true)
    public void stop() throws IOException {
        this.close();
    }
}
