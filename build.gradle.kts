plugins {
    java
    `maven-publish`
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("me.champeau.jmh") version "0.7.3"
}

group = "com.github.groundbreakingmc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "enginehub"
        url = uri("https://maven.enginehub.org/repo/")
    }
    maven {
        name = "placeholder-api"
        url = uri("https://repo.extendedclip.com/releases/")
    }
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {

    // MINECRAFT

    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    // https://github.com/MilkBowl/VaultAPI
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    // https://luckperms.net/wiki/Developer-API
    compileOnly("net.luckperms:api:5.4")

    // https://mvnrepository.com/artifact/com.mojang/authlib
    compileOnly("com.mojang:authlib:3.13.56")

    // https://worldguard.enginehub.org/en/latest/developer/dependency/
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.5")

    // https://www.spigotmc.org/resources/supervanish-be-invisible.1331/
    compileOnly("com.github.LeonMangler:SuperVanish:6.2.19")

    // https://wiki.placeholderapi.com/developers/using-placeholderapi/
    compileOnly("me.clip:placeholderapi:2.11.6")

    // https://mvnrepository.com/artifact/net.kyori/adventure-text-minimessage
    compileOnly("net.kyori:adventure-text-minimessage:4.24.0")

    // https://mvnrepository.com/artifact/net.kyori/adventure-text-logger-slf4j
    compileOnly("net.kyori:adventure-text-logger-slf4j:4.24.0")

    // OTHER

    // https://mvnrepository.com/artifact/org.spongepowered/configurate-yaml
    compileOnly("org.spongepowered:configurate-yaml:4.2.0")

    // https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.2")

    // https://mvnrepository.com/artifact/com.google.guava/guava
    compileOnly("com.google.guava:guava:33.4.8-jre")

    // https://mvnrepository.com/artifact/it.unimi.dsi/fastutil
    compileOnly("it.unimi.dsi:fastutil:8.5.16")

    // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
    compileOnly("com.zaxxer:HikariCP:7.0.0")

    // https://mvnrepository.com/artifact/com.typesafe/config
    compileOnly("com.typesafe:config:1.4.3")

    // ANNOTATIONS

    // https://mvnrepository.com/artifact/org.projectlombok/lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // TESTS

    // https://mvnrepository.com/artifact/org.junit
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // https://mvnrepository.com/artifact/org.openjdk.jmh/jmh-core
    jmh("org.openjdk.jmh:jmh-core:1.37")
    // https://mvnrepository.com/artifact/org.openjdk.jmh/jmh-generator-annprocess
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

tasks.register<JavaExec>("jmhWarmup") {
    group = "benchmark"
    description = "Run JMH benchmarks with warmup"
    classpath = tasks.jmhJar.get().outputs.files + configurations.jmh.get()
    mainClass.set("org.openjdk.jmh.Main")
    args = listOf(
            "-bm", "AverageTime", // BenchmarkMode
            "-tu", "ns",          // TimeUnit: ns
            "-wi", "3",           // Warmup iterations
            "-i", "5",            // Measurement iteration
            "-t", "5",            // Measurement time
            "-f", "3",            // Forks
    )
}

tasks.register<JavaExec>("jmhCold") {
    group = "benchmark"
    description = "Run JMH benchmarks without warmup (cold state)"
    classpath = tasks.jmhJar.get().outputs.files + configurations.jmh.get()
    mainClass.set("org.openjdk.jmh.Main")
    args = listOf(
            "-bm", "SingleShotTime", // BenchmarkMode
            "-tu", "ns", // TimeUnit: ns
            "-wi", "0",  // Warmup iterations
            "-i", "1",   // Measurement iteration
            "-f", "3",   // Forks
    )
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = "MyLib"
                description = "A lightweight and extensible Java library tailored for Minecraft plugin development."
                url = "https://github.com/groundbreakingmc/MyLib"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "GroundbreakingMC"
                        name = "Victor"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/groundbreakingmc/MyLib.git"
                    developerConnection = "scm:git:ssh://git@github.com:groundbreakingmc/MyLib.git"
                    url = "https://github.com/groundbreakingmc/MyLib"
                }
            }
        }
    }
}

bukkit {
    description = "A lightweight and extensible Java library tailored for Minecraft plugin development."

    main = "com.github.groundbreakingmc.mylib.MyLib"
    apiVersion = "1.13"

    author = "GroundbreakingMC"
    contributors = listOf("OverwriteMC")
    website = "https//github.com/groundbreakingmc/MyLib"

    softDepend = listOf("WorldGuard", "LuckPerms", "Vault", "UltimateVanish", "SuperVanish", "PremiumVanish")

    libraries = listOf("com.github.ben-manes.caffeine:caffeine:3.2.2",
            "org.spongepowered:configurate-yaml:4.0.0",
            "com.zaxxer:HikariCP:7.0.0",
            "com.typesafe:config:1.4.3"
    )
}

tasks {
    withType<JavaCompile> {
        options.release = 21
        options.encoding = "UTF-8"
    }

    register("buildPlugin") {
        dependsOn("build")
    }

    named("generateBukkitPluginDescription") {
        onlyIf { gradle.startParameter.taskNames.contains("buildPlugin") }
    }

    test {
        useJUnitPlatform()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
