plugins {
    java
    `maven-publish`
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
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

    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")

    // https://github.com/MilkBowl/VaultAPI
    implementation("com.github.MilkBowl:VaultAPI:1.7.1")

    // https://luckperms.net/wiki/Developer-API
    compileOnly("net.luckperms:api:5.4")

    // https://mvnrepository.com/artifact/com.mojang/authlib
    compileOnly("com.mojang:authlib:3.13.56")

    // https://worldguard.enginehub.org/en/latest/developer/dependency/
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.5")

    // https://www.spigotmc.org/resources/supervanish-be-invisible.1331/
    implementation("com.github.LeonMangler:SuperVanish:6.2.19")

    // https://wiki.placeholderapi.com/developers/using-placeholderapi/
    compileOnly("me.clip:placeholderapi:2.11.6")

    // https://mvnrepository.com/artifact/net.kyori/adventure-text-minimessage
    implementation("net.kyori:adventure-text-minimessage:4.24.0")

    // https://mvnrepository.com/artifact/net.kyori/adventure-text-logger-slf4j
    implementation("net.kyori:adventure-text-logger-slf4j:4.24.0")

    // OTHER

    // https://mvnrepository.com/artifact/org.spongepowered/configurate-yaml
    implementation("org.spongepowered:configurate-yaml:4.2.0")

    // https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.2")

    // https://mvnrepository.com/artifact/com.google.guava/guava
    implementation("com.google.guava:guava:33.4.8-jre")

    // https://mvnrepository.com/artifact/it.unimi.dsi/fastutil
    implementation("it.unimi.dsi:fastutil:8.5.16")

    // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
    implementation("com.zaxxer:HikariCP:7.0.0")

    // https://mvnrepository.com/artifact/com.typesafe/config
    implementation("com.typesafe:config:1.4.3")

    // ANNOTATIONS

    // https://mvnrepository.com/artifact/org.projectlombok/lombok
    implementation("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // TESTS

    // https://mvnrepository.com/artifact/org.junit
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
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

    depend = listOf("PlaceholderAPI")
    softDepend = listOf("WorldGuard", "LuckPerms", "Vault", "UltimateVanish", "SuperVanish", "PremiumVanish")

    libraries = listOf("com.github.ben-manes.caffeine:caffeine:3.2.2",
            "org.spongepowered:configurate-yaml:4.0.0",
            "com.zaxxer:HikariCP:7.0.0",
            "com.typesafe:config:1.4.3"
    )
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("buildPlugin") {
    dependsOn("build")
    doLast {
        println("Built with plugin-yml")
    }
}

tasks.named("generateBukkitPluginDescription") {
    onlyIf { gradle.startParameter.taskNames.contains("buildPlugin") }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
