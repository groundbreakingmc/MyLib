plugins {
    id("mylib.module")
    id("mylib.jmh")
    alias(libs.plugins.plugin.yml.bukkit)
}

description = "A lightweight and extensible Java library tailored for Minecraft plugin development"

// The plugin jar below takes the classes of mylib-kv, so that project is configured first.
evaluationDependsOn(":mylib-kv")

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://jitpack.io")
}

dependencies {
    // The key-value store, published alone too: it doesn't need Bukkit.
    api(project(":mylib-kv"))

    // Minecraft
    compileOnly(libs.paper.api)
    testImplementation(libs.paper.api)
    compileOnly(libs.bungeecord.api)

    // Plugins
    compileOnly(libs.vault.api)
    compileOnly(libs.luckperms.api)
    compileOnly(libs.worldguard.bukkit) {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }
    compileOnly(libs.supervanish)
    compileOnly(libs.placeholderapi)

    // Mojang
    compileOnly(libs.authlib)

    // Adventure
    compileOnly(libs.adventure.text.minimessage)
    compileOnly(libs.adventure.text.logger.slf4j)

    // Utilities
    compileOnly(libs.configurate.yaml)
    compileOnly(libs.configurate.hocon)
    compileOnly(libs.caffeine)
    compileOnly(libs.guava)
    compileOnly(libs.fastutil)
    testImplementation(libs.fastutil)
    compileOnly(libs.hikaricp)
    compileOnly(libs.typesafe.config)

    // Annotations
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.jetbrains.annotations)

    // Testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockbukkit)

    // Benchmarking
    jmh(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.generator.annprocess)
}

tasks {
    javadoc {
        (options as StandardJavadocDocletOptions).links("https://jd.papermc.io/paper/1.21/")
    }

    register("buildPlugin") {
        group = "build"
        description = "Build plugin with bukkit.yml generation"
        dependsOn("build")
    }

    named("generateBukkitPluginDescription") {
        onlyIf { gradle.startParameter.taskNames.contains("buildPlugin") }
    }

    jar {
        // The standalone plugin carries the key-value store itself; libraries get it as a dependency.
        if (gradle.startParameter.taskNames.contains("buildPlugin")) {
            from(project(":mylib-kv").the<SourceSetContainer>()["main"].output)
        }
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
