package com.github.groundbreakingmc.mylib.utils.pdc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HashCollisionTest {

    @FunctionalInterface
    interface ToIntTriFunction {
        int apply(int x, int y, int z);
    }

    static class HashFunctions {
        static int stringConcatenation(int x, int y, int z) {
            return ("x" + x + "y" + y + "z" + z).hashCode();
        }

        static int bitwiseShiftXOR(int x, int y, int z) {
            return x ^ (y << 10) ^ (z << 20);
        }

        static int bitwisePacking(int x, int y, int z) {
            return (x & 0xFFF) | ((y & 0xFFF) << 12) | ((z & 0xFFF) << 24);
        }

        static int xorMultiplication(int x, int y, int z) {
            return x ^ (y * 31) ^ (z * 31 * 31);
        }

        static int cantorPairing(int x, int y, int z) {
            int hash = ((x + z) * (x + z + 1) / 2) + z;
            hash = ((hash + y) * (hash + y + 1) / 2) + y;
            return hash;
        }

        static int objectsHash(int x, int y, int z) {
            return Objects.hash(x, y, z);
        }

        static int murmurLike(int x, int y, int z) {
            int hash = x;
            hash = 31 * hash + y;
            hash = 31 * hash + z;
            return hash;
        }

        static int spatialHashPrimes(int x, int y, int z) {
            return x * 73856093 ^ y * 19349663 ^ z * 83492791;
        }

        static int longBasedHash(int x, int y, int z) {
            long hash = ((long) x << 32) | ((long) y << 16) | z;
            return (int) (hash ^ (hash >>> 32));
        }

        static int bitwiseShiftPacking(int x, int y, int z) {
            return (y << 8) | (z << 4) | x;
        }
    }

    static Stream<TestCase> hashFunctionProvider() {
        return Stream.of(
                new TestCase("String Concatenation", HashFunctions::stringConcatenation),
                new TestCase("Bitwise Shift XOR", HashFunctions::bitwiseShiftXOR),
                new TestCase("Bitwise Packing", HashFunctions::bitwisePacking),
                new TestCase("XOR Multiplication", HashFunctions::xorMultiplication), // Failed
                new TestCase("Cantor Pairing", HashFunctions::cantorPairing), // Failed
                new TestCase("Objects.hash()", HashFunctions::objectsHash), // Failed
                new TestCase("MurmurHash-like", HashFunctions::murmurLike), // Failed
                new TestCase("Spatial Hash Primes", HashFunctions::spatialHashPrimes),
                new TestCase("Long-based Hash", HashFunctions::longBasedHash), // Failed
                new TestCase("Bitwise Shift Packing", HashFunctions::bitwiseShiftPacking)
        );
    }

    static class TestCase {
        String name;
        ToIntTriFunction hashFunc;

        TestCase(String name, ToIntTriFunction hashFunc) {
            this.name = name;
            this.hashFunc = hashFunc;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("hashFunctionProvider")
    void testNoCollisions(TestCase testCase) {
        record Pos(int x, int y, int z) {
        }

        final Map<Integer, Pos> hashes = new HashMap<>();
        final List<String> collisions = new ArrayList<>();

        start:
        for (int x = -16; x < 16; x++) {
            for (int y = -64; y < 300; y++) {
                for (int z = -16; z < 16; z++) {
                    final int localX = x & 0xF;
                    final int localZ = z & 0xF;
                    final int hash = testCase.hashFunc.apply(localX, y, localZ);
                    final Pos newPos = new Pos(localX, y, localZ);
                    final Pos oldPos = hashes.put(hash, newPos);
                    if (oldPos != null && !oldPos.equals(newPos)) {
                        collisions.add(String.format("Collision at (%d, %d, %d) with (%d, %d, %d)", localX, y, localZ, oldPos.x, oldPos.y, oldPos.z));
                        if (collisions.size() == 10) break start;
                    }
                }
            }
        }

        if (!collisions.isEmpty()) {
            System.out.println("\n" + testCase.name + " collisions found:");
            collisions.forEach(System.out::println);
        }

        assertTrue(collisions.isEmpty(),
                testCase.name + " has " + collisions.size() + " collisions in small range");
    }
}
