package com.github.groundbreakingmc.mylib.database.kv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DiskRepositoryTest {

    // -------------------------------------------------------------------------
    // Encoders
    // -------------------------------------------------------------------------

    private static final KeyValueEncoder<String> STRING_ENCODER = new KeyValueEncoder<>() {
        public byte[] encode(String value) {
            return value.getBytes(StandardCharsets.UTF_8);
        }

        public String decode(byte[] data) {
            return new String(data, StandardCharsets.UTF_8);
        }
    };

    private static final KeyValueEncoder<UUID> UUID_ENCODER = new KeyValueEncoder<>() {
        public byte[] encode(UUID value) {
            var buf = java.nio.ByteBuffer.allocate(16);
            buf.putLong(value.getMostSignificantBits());
            buf.putLong(value.getLeastSignificantBits());
            return buf.array();
        }

        public UUID decode(byte[] data) {
            var buf = java.nio.ByteBuffer.wrap(data);
            return new UUID(buf.getLong(), buf.getLong());
        }
    };

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private KVRepository<String, String> open(Path dir) {
        return open(dir, RepositoryConfig.defaults());
    }

    private KVRepository<String, String> open(Path dir, RepositoryConfig config) {
        return KVRepository.<String, String>builder()
                .path(dir.resolve("test.db"))
                .keyEncoder(STRING_ENCODER)
                .valueEncoder(STRING_ENCODER)
                .config(config)
                .build();
    }

    /**
     * Polls up to 2 seconds for the writer thread to flush the expected value.
     */
    private static <K, V> void assertEventuallyEquals(KVRepository<K, V> repo, K key, V expected) {
        long deadline = System.currentTimeMillis() + 2_000;
        while (System.currentTimeMillis() < deadline) {
            if (repo.load(key).map(expected::equals).orElse(false)) return;
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        assertEquals(Optional.of(expected), repo.load(key));
    }

    private static <K, V> void assertEventuallyEmpty(KVRepository<K, V> repo, K key) {
        long deadline = System.currentTimeMillis() + 2_000;
        while (System.currentTimeMillis() < deadline) {
            if (repo.load(key).isEmpty()) return;
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        assertEquals(Optional.empty(), repo.load(key));
    }

    // -------------------------------------------------------------------------
    // Basic CRUD
    // -------------------------------------------------------------------------

    @Test
    void saveAndLoad(@TempDir Path dir) {
        var repo = open(dir);
        repo.save("player1", "Alice");
        assertEventuallyEquals(repo, "player1", "Alice");
        repo.close();
    }

    @Test
    void loadReturnsEmptyForMissingKey(@TempDir Path dir) {
        var repo = open(dir);
        assertEquals(Optional.empty(), repo.load("ghost"));
        repo.close();
    }

    @Test
    void saveOverwritesPreviousValue(@TempDir Path dir) {
        var repo = open(dir);
        repo.save("key", "first");
        repo.save("key", "second");
        assertEventuallyEquals(repo, "key", "second");
        repo.close();
    }

    @Test
    void deleteRemovesKey(@TempDir Path dir) {
        var repo = open(dir);
        repo.save("key", "value");
        repo.delete("key");
        assertEventuallyEmpty(repo, "key");
        repo.close();
    }

    @Test
    void deleteNonExistentKeyDoesNotThrow(@TempDir Path dir) {
        var repo = open(dir);
        assertDoesNotThrow(() -> repo.delete("ghost"));
        repo.close();
    }

    @Test
    void multipleDifferentKeys(@TempDir Path dir) {
        var repo = open(dir);
        repo.save("a", "1");
        repo.save("b", "2");
        repo.save("c", "3");
        assertEventuallyEquals(repo, "a", "1");
        assertEventuallyEquals(repo, "b", "2");
        assertEventuallyEquals(repo, "c", "3");
        repo.close();
    }

    // -------------------------------------------------------------------------
    // Persistence across restarts
    // -------------------------------------------------------------------------

    @Test
    void dataPersistedAfterReopen(@TempDir Path dir) {
        var repo = open(dir);
        repo.save("key", "persisted");
        repo.close();

        var repo2 = open(dir);
        assertEquals(Optional.of("persisted"), repo2.load("key"));
        repo2.close();
    }

    @Test
    void deletePersistedAfterReopen(@TempDir Path dir) {
        var repo = open(dir);
        repo.save("key", "value");
        repo.delete("key");
        repo.close();

        var repo2 = open(dir);
        assertEquals(Optional.empty(), repo2.load("key"));
        repo2.close();
    }

    @Test
    void updatePersistedAfterReopen(@TempDir Path dir) {
        var repo = open(dir);
        repo.save("key", "old");
        repo.save("key", "new");
        repo.close();

        var repo2 = open(dir);
        assertEquals(Optional.of("new"), repo2.load("key"));
        repo2.close();
    }

    // -------------------------------------------------------------------------
    // Cache
    // -------------------------------------------------------------------------

    @Test
    void cacheReturnsSavedValueImmediately(@TempDir Path dir) {
        var repo = KVRepository.<String, String>builder()
                .path(dir.resolve("test.db"))
                .keyEncoder(STRING_ENCODER)
                .valueEncoder(STRING_ENCODER)
                .config(RepositoryConfig.withCache())
                .build();

        repo.save("key", "cached");
        // no wait — cache must return value synchronously
        assertEquals(Optional.of("cached"), repo.load("key"));
        repo.close();
    }

    @Test
    void unloadFromCacheDoesNotDeleteFromDisk(@TempDir Path dir) {
        var repo = KVRepository.<String, String>builder()
                .path(dir.resolve("test.db"))
                .keyEncoder(STRING_ENCODER)
                .valueEncoder(STRING_ENCODER)
                .config(RepositoryConfig.withCache())
                .build();

        repo.save("key", "value");
        repo.close();

        var repo2 = KVRepository.<String, String>builder()
                .path(dir.resolve("test.db"))
                .keyEncoder(STRING_ENCODER)
                .valueEncoder(STRING_ENCODER)
                .config(RepositoryConfig.withCache())
                .build();

        repo2.unloadFromCache("key");
        assertEquals(Optional.of("value"), repo2.load("key")); // still on disk
        repo2.close();
    }

    // -------------------------------------------------------------------------
    // File lock
    // -------------------------------------------------------------------------

    @Test
    void openingSameFileTwiceThrows(@TempDir Path dir) {
        var repo = open(dir);
        try {
            assertThrows(IllegalStateException.class, () -> open(dir));
        } finally {
            repo.close();
        }
    }

    @Test
    void canReopenAfterClose(@TempDir Path dir) {
        var repo = open(dir);
        repo.close();

        assertDoesNotThrow(() -> {
            var repo2 = open(dir);
            repo2.close();
        });
    }

    // -------------------------------------------------------------------------
    // Concurrency
    // -------------------------------------------------------------------------

    @Test
    void concurrentSavesAllPersisted(@TempDir Path dir) throws Exception {
        var repo = open(dir);
        int threadCount = 8;
        int writesPerThread = 50;

        var latch = new CountDownLatch(threadCount);
        var executor = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            int tid = t;
            executor.submit(() -> {
                for (int i = 0; i < writesPerThread; i++) {
                    repo.save("thread-" + tid + "-key-" + i, "value-" + i);
                }
                latch.countDown();
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        repo.close();

        var repo2 = open(dir);
        for (int t = 0; t < threadCount; t++) {
            for (int i = 0; i < writesPerThread; i++) {
                assertEquals(
                        Optional.of("value-" + i),
                        repo2.load("thread-" + t + "-key-" + i),
                        "Missing: thread-" + t + "-key-" + i
                );
            }
        }
        repo2.close();
    }

    @Test
    void concurrentReadsWhileWriting(@TempDir Path dir) throws Exception {
        var repo = open(dir);
        repo.save("stable", "present");
        repo.close();

        var repo2 = open(dir);
        int readerCount = 4;
        var latch = new CountDownLatch(readerCount);
        var executor = Executors.newFixedThreadPool(readerCount + 1);

        executor.submit(() -> {
            for (int i = 0; i < 100; i++) {
                repo2.save("key-" + i, "val-" + i);
            }
        });

        for (int r = 0; r < readerCount; r++) {
            executor.submit(() -> {
                for (int i = 0; i < 200; i++) {
                    assertDoesNotThrow(() -> repo2.load("stable"));
                }
                latch.countDown();
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        repo2.close();
    }

    // -------------------------------------------------------------------------
    // Compaction
    // -------------------------------------------------------------------------

    @Test
    void compactionPreservesAllLiveData(@TempDir Path dir) {
        var config = new RepositoryConfig(false, 64, 0.01, 1L);
        var repo = open(dir, config);

        for (int i = 0; i < 20; i++) {
            repo.save("key-" + i, "value-" + i);
        }
        for (int i = 0; i < 10; i++) {
            repo.save("key-" + i, "updated-" + i);
        }
        repo.close();

        var repo2 = open(dir);
        for (int i = 0; i < 10; i++) {
            assertEquals(Optional.of("updated-" + i), repo2.load("key-" + i));
        }
        for (int i = 10; i < 20; i++) {
            assertEquals(Optional.of("value-" + i), repo2.load("key-" + i));
        }
        repo2.close();
    }

    @Test
    void compactionRemovesDeletedKeys(@TempDir Path dir) {
        var config = new RepositoryConfig(false, 64, 0.01, 1L);
        var repo = open(dir, config);

        repo.save("alive", "yes");
        repo.save("dead", "bye");
        repo.delete("dead");
        repo.close();

        var repo2 = open(dir);
        assertEquals(Optional.of("yes"), repo2.load("alive"));
        assertEquals(Optional.empty(), repo2.load("dead"));
        repo2.close();
    }

    // -------------------------------------------------------------------------
    // Builder — lambda encoders
    // -------------------------------------------------------------------------

    @Test
    void builderLambdaEncoders(@TempDir Path dir) {
        var repo = KVRepository.<UUID, String>builder()
                .path(dir.resolve("uuid.db"))
                .keyEncoder(UUID_ENCODER)
                .valueEncoder(
                        s -> s.getBytes(StandardCharsets.UTF_8),
                        b -> new String(b, StandardCharsets.UTF_8)
                )
                .build();

        UUID id = UUID.randomUUID();
        repo.save(id, "player");
        assertEventuallyEquals(repo, id, "player");
        repo.close();
    }

    // -------------------------------------------------------------------------
    // Config validation
    // -------------------------------------------------------------------------

    @Test
    void configRejectsInvalidBatchSize() {
        assertThrows(IllegalArgumentException.class,
                () -> new RepositoryConfig(false, 0, 0.2, 1024));
    }

    @Test
    void configRejectsInvalidCompactRatio() {
        assertThrows(IllegalArgumentException.class,
                () -> new RepositoryConfig(false, 64, 1.0, 1024));
        assertThrows(IllegalArgumentException.class,
                () -> new RepositoryConfig(false, 64, 0.0, 1024));
    }

    @Test
    void configRejectsNegativeCompactBytes() {
        assertThrows(IllegalArgumentException.class,
                () -> new RepositoryConfig(false, 64, 0.2, -1));
    }
}
