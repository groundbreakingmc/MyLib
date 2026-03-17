package com.github.groundbreakingmc.mylib.database.kv.encoders.utils;

public final class ByteUtils {

    private ByteUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void putInt(byte[] bytes, int offset, int value) {
        for (int i = 3; i >= 0; i--) {
            bytes[offset + i] = (byte) (value & 0xFF);
            value >>= 8;
        }
    }

    public static int bytesToInt(byte[] bytes, int offset) {
        int result = 0;
        for (int i = 0; i < 3; i++) {
            result <<= 8;
            result |= (bytes[offset + i] & 0xFF);
        }
        return result;
    }

    public static void putLong(byte[] bytes, int offset, long value) {
        for (int i = 7; i >= 0; i--) {
            bytes[offset + i] = (byte) (value & 0xFF);
            value >>= 8;
        }
    }

    public static long bytesToLong(byte[] bytes, int offset) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (bytes[offset + i] & 0xFF);
        }
        return result;
    }

    public static void putDouble(byte[] bytes, int offset, double value) {
        putLong(bytes, offset, Double.doubleToLongBits(value));
    }

    public static double bytesToDouble(byte[] bytes, int offset) {
        return Double.longBitsToDouble(bytesToLong(bytes, offset));
    }

    public static void putFloat(byte[] bytes, int offset, float value) {
        putInt(bytes, offset, Float.floatToIntBits(value));
    }

    public static float bytesToFloat(byte[] bytes, int offset) {
        return Float.intBitsToFloat(bytesToInt(bytes, offset));
    }
}
