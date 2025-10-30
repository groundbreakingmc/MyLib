package com.github.groundbreakingmc.mylib.utils.pdc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HashCollisionTest {

    @Test
    @DisplayName("Hash Collision Test")
    void testNoCollisions() {
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
                    final int hash = generateBlockHash(localX, y, localZ);
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
            System.out.println("collisions found:");
            collisions.forEach(System.out::println);
        }

        assertTrue(collisions.isEmpty(), "Found " + collisions.size() + " collisions in one chunk");
    }

    public int generateBlockHash(int x, int y, int z) {
        x &= 0xF;
        z &= 0xF;
        return (y << 8) | (z << 4) | x;
    }
}
