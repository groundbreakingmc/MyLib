package com.github.groundbreakingmc.mylib;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;

@State(Scope.Benchmark)
public class CoordinateHashBenchmarkWarmup {

    private int x, y, z;

    @Setup
    public void setup() {
        final Random random = new Random(42);
        this.x = random.nextInt(32) - 16;
        this.y = random.nextInt(364) - 64;
        this.z = random.nextInt(32) - 16;
    }

    @Benchmark
    public void stringConcatenation(Blackhole bh) {
        int hash = ("x" + x + "y" + y + "z" + z).hashCode();
        bh.consume(hash);
    }

    @Benchmark
    public void bitwiseShiftXOR(Blackhole bh) {
        int hash = x ^ (y << 10) ^ (z << 20);
        bh.consume(hash);
    }

    @Benchmark
    public void bitwisePacking(Blackhole bh) {
        int hash = (x & 0xFFF) | ((y & 0xFFF) << 12) | ((z & 0xFFF) << 24);
        bh.consume(hash);
    }

    @Benchmark
    public void spatialHashPrimes(Blackhole bh) {
        int hash = x * 73856093 ^ y * 19349663 ^ z * 83492791;
        bh.consume(hash);
    }

    @Benchmark // fastest
    public void bitwiseShiftPacking(Blackhole bh) {
        int hash = (y << 8) | (z << 4) | x;
        bh.consume(hash);
    }
}
