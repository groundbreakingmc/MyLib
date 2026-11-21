// JMH benchmarks from src/jmh, with presets for runs with and without warmup.
plugins {
    java
    id("me.champeau.jmh")
}

tasks {
    register<JavaExec>("jmhWarmup") {
        group = "benchmark"
        description = "Run JMH benchmarks with warmup"
        classpath = files(jmhJar, configurations.jmh)
        mainClass = "org.openjdk.jmh.Main"
        args(
                "-bm", "AverageTime",
                "-tu", "ns",
                "-wi", "3",
                "-i", "5",
                "-t", "5",
                "-f", "3"
        )
    }

    register<JavaExec>("jmhCold") {
        group = "benchmark"
        description = "Run JMH benchmarks without warmup (cold state)"
        classpath = files(jmhJar, configurations.jmh)
        mainClass = "org.openjdk.jmh.Main"
        args(
                "-bm", "SingleShotTime",
                "-tu", "ns",
                "-wi", "0",
                "-i", "1",
                "-f", "3"
        )
    }
}
