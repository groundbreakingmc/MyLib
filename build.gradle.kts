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
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://jitpack.io")
}

dependencies {
    // Minecraft
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.21-R0.3")

    // Plugins
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.5") {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }
    compileOnly("com.github.LeonMangler:SuperVanish:6.2.19")
    compileOnly("me.clip:placeholderapi:2.11.6")

    // Mojang
    compileOnly("com.mojang:authlib:3.13.56")

    // Adventure
    compileOnly("net.kyori:adventure-text-minimessage:4.24.0")
    compileOnly("net.kyori:adventure-text-logger-slf4j:4.24.0")

    // Utilities
    compileOnly("org.spongepowered:configurate-yaml:4.2.0")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.2")
    compileOnly("com.google.guava:guava:33.4.8-jre")
    compileOnly("it.unimi.dsi:fastutil:8.5.16")
    compileOnly("com.zaxxer:HikariCP:7.0.0")
    compileOnly("com.typesafe:config:1.4.3")

    // Annotations
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    compileOnly("org.jetbrains:annotations:26.0.1")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Benchmarking
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    javadoc {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).apply {
            links("https://docs.oracle.com/en/java/javase/21/docs/api/")
            links("https://jd.papermc.io/paper/1.21/")
        }
    }

    test {
        useJUnitPlatform()
    }

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

    register("buildPlugin") {
        group = "build"
        description = "Build plugin with bukkit.yml generation"
        dependsOn("build")
    }

    named("generateBukkitPluginDescription") {
        onlyIf { gradle.startParameter.taskNames.contains("buildPlugin") }
    }
}

bukkit {
    main = "com.github.groundbreakingmc.mylib.MyLib"
    apiVersion = "1.13"

    name = "MyLib"
    description = "A lightweight and extensible Java library tailored for Minecraft plugin development"
    website = "https://github.com/groundbreakingmc/MyLib"

    author = "GroundbreakingMC"
    contributors = listOf("OverwriteMC")

    softDepend = listOf(
            "WorldGuard",
            "LuckPerms",
            "Vault",
            "UltimateVanish",
            "SuperVanish",
            "PremiumVanish"
    )

    libraries = listOf(
            "com.github.ben-manes.caffeine:caffeine:3.2.2",
            "org.spongepowered:configurate-yaml:4.2.0",
            "com.zaxxer:HikariCP:7.0.0",
            "com.typesafe:config:1.4.3"
    )
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = "mylib"
            version = project.version.toString()

            pom {
                name = "MyLib"
                description = "A lightweight and extensible Java library tailored for Minecraft plugin development"
                url = "https://github.com/groundbreakingmc/MyLib"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "GroundbreakingMC"
                        name = "Victor"
                        url = "https://github.com/groundbreakingmc"
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

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/groundbreakingmc/MyLib")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}