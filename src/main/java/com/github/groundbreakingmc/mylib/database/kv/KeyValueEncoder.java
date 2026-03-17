package com.github.groundbreakingmc.mylib.database.kv;

/**
 * Converts values of type {@code T} to and from a byte array representation
 * for storage on disk.
 *
 * <p>Implementations must ensure that {@code decode(encode(value))} returns
 * a value equal to the original. The byte format is entirely up to the
 * implementation — JSON, protobuf, a custom binary format, etc.
 *
 * <p>Example for {@code UUID}:
 * <pre>{@code
 * public class UUIDEncoder implements KeyValueEncoder<UUID> {
 *
 *     public static final UUIDEncoder INSTANCE = new UUIDEncoder();
 *
 *     @Override
 *     public byte[] encode(UUID uuid) {
 *         ByteBuffer buf = ByteBuffer.allocate(16);
 *         buf.putLong(uuid.getMostSignificantBits());
 *         buf.putLong(uuid.getLeastSignificantBits());
 *         return buf.array();
 *     }
 *
 *     @Override
 *     public UUID decode(byte[] data) {
 *         ByteBuffer buf = ByteBuffer.wrap(data);
 *         return new UUID(buf.getLong(), buf.getLong());
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type to encode
 */
public interface KeyValueEncoder<T> {

    /**
     * Encodes the given value into a byte array.
     *
     * @param value the value to encode
     * @return a byte array representing the value, never null
     */
    byte[] encode(T value);

    /**
     * Decodes a byte array back into a value of type {@code T}.
     *
     * @param data the byte array to decode
     * @return the decoded value, never null
     */
    T decode(byte[] data);
}
