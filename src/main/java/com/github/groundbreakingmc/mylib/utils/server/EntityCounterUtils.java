package com.github.groundbreakingmc.mylib.utils.server;

import com.github.groundbreakingmc.mylib.utils.server.version.ServerVersion;
import com.github.groundbreakingmc.mylib.utils.server.version.ServerVersionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for generating unique entity IDs by accessing the internal Minecraft server
 * entity counter through reflection.
 *
 * <p>This class provides a version-agnostic way to obtain unique entity IDs by accessing
 * the internal entity counter used by the Minecraft server. It automatically selects the
 * appropriate implementation based on the server version:
 * <ul>
 *   <li>For servers 1.14+ (V1_14_R1 and higher): Uses {@link AtomicEntityCounter}</li>
 *   <li>For older servers: Uses {@link SimpleEntityCounter}</li>
 * </ul>
 *
 * <p><strong>Warning:</strong> This utility relies on NMS (Net Minecraft Server) internals
 * which may change between Minecraft versions. Use with caution in production environments.
 *
 * @author GroundbreakingMC
 * @since 1.0
 */
public final class EntityCounterUtils {

    /**
     * The entity counter instance used for generating unique IDs.
     */
    private static final EntityCounter ENTITY_COUNTER;

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always, as this class should not be instantiated
     */
    private EntityCounterUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Generates and returns the next unique entity ID.
     *
     * <p>This method is thread-safe and guarantees that each call will return a unique,
     * incrementing integer value that corresponds to the next entity ID that would be
     * assigned by the Minecraft server.
     *
     * @return the next unique entity ID
     * @throws RuntimeException if the underlying entity counter fails to generate an ID
     */
    public static int nextId() {
        return ENTITY_COUNTER.nextId();
    }

    static {
        ENTITY_COUNTER = ServerVersionUtils.serverVersion().isHigherOrEqual(ServerVersion.V1_14_R1)
                ? new AtomicEntityCounter()
                : new SimpleEntityCounter();
    }

    /**
     * Internal interface defining the contract for entity counter implementations.
     */
    private interface EntityCounter {

        /**
         * Generates and returns the next unique entity ID.
         *
         * @return the next unique entity ID
         */
        int nextId();
    }

    /**
     * Entity counter implementation for Minecraft servers version 1.14 and higher.
     *
     * <p>This implementation uses reflection to access the {@link AtomicInteger} field
     * that stores the entity counter in the Minecraft server's Entity class. It leverages
     * the atomic operations provided by {@link AtomicInteger} to ensure thread safety.
     *
     * <p>The field name varies by version:
     * <ul>
     *   <li>1.17+ (V1_17_R1 and higher): "ENTITY_COUNTER"</li>
     *   <li>1.14-1.16 (V1_14_R1 to V1_16_R3): "entityCount"</li>
     * </ul>
     */
    static class AtomicEntityCounter implements EntityCounter {

        /**
         * The atomic integer counter obtained from the Minecraft server's Entity class.
         */
        private final AtomicInteger counter;

        /**
         * Constructs a new AtomicEntityCounter by accessing the server's internal
         * entity counter through reflection.
         *
         * @throws RuntimeException if reflection fails or the required field cannot be found
         */
        private AtomicEntityCounter() {
            try {
                final Class<?> entityClass = NmsUtils.nmsClass("world.entity.Entity", "Entity");
                Objects.requireNonNull(entityClass);

                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(entityClass, MethodHandles.lookup());

                final MethodHandle getter = ServerVersionUtils.serverVersion().isHigherOrEqual(ServerVersion.V1_17_R1)
                        ? lookup.findGetter(entityClass, "ENTITY_COUNTER", AtomicInteger.class)
                        : lookup.findGetter(entityClass, "entityCount", AtomicInteger.class);

                this.counter = (AtomicInteger) getter.invoke(null);
            } catch (Throwable th) {
                throw new RuntimeException(th);
            }
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation uses {@link AtomicInteger#incrementAndGet()} to ensure
         * atomic increment operation and thread safety.
         *
         * @return {@inheritDoc}
         */
        @Override
        public int nextId() {
            return this.counter.incrementAndGet();
        }
    }

    /**
     * Entity counter implementation for Minecraft servers older than version 1.14.
     *
     * <p>This implementation uses {@link VarHandle} to access and modify the primitive
     * {@code int} field that stores the entity counter in older Minecraft server versions.
     * It implements a compare-and-swap loop to ensure thread safety when incrementing
     * the counter.
     */
    static class SimpleEntityCounter implements EntityCounter {

        /**
         * The VarHandle for accessing the primitive int counter field.
         */
        private final VarHandle counter;

        /**
         * Constructs a new SimpleEntityCounter by accessing the server's internal
         * entity counter through reflection.
         *
         * @throws RuntimeException if reflection fails or the required field cannot be found
         */
        private SimpleEntityCounter() {
            try {
                final Class<?> entityClass = NmsUtils.nmsClass("world.entity.Entity", "Entity");
                Objects.requireNonNull(entityClass);

                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(entityClass, MethodHandles.lookup());

                this.counter = lookup.findVarHandle(entityClass, "entityCount", int.class);
            } catch (Throwable th) {
                throw new RuntimeException(th);
            }
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation uses a compare-and-swap loop with {@link VarHandle#getVolatile(Object...)}
         * and {@link VarHandle#compareAndSet(Object...)} to ensure atomic increment operation
         * and thread safety when dealing with primitive int counters.
         *
         * @return {@inheritDoc}
         */
        @Override
        public int nextId() {
            int current, next;
            do {
                current = (int) this.counter.getVolatile(null);
                next = current + 1;
            } while (!this.counter.compareAndSet(null, current, next));
            return next;
        }
    }
}
