plugins {
    id("mylib.module")
    id("mylib.jmh")
}

description = "A lightweight and extensible Java library tailored for Minecraft plugin development"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io")
}

dependencies {
    // The modules without Bukkit, published alone too.
    api(project(":mylib-eventbus"))
    api(project(":mylib-sql"))
    api(project(":mylib-kv"))
    api(project(":mylib-colorizer"))
    api(project(":mylib-config"))
    api(project(":mylib-logger"))

    // Minecraft
    compileOnly(libs.paper.api)
    testImplementation(libs.paper.api)

    // Plugins
    compileOnly(libs.vault.api)
    compileOnly(libs.luckperms.api)
    compileOnly(libs.worldguard.bukkit) {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }

    // Mojang
    compileOnly(libs.authlib)

    // Utilities
    compileOnly(libs.caffeine)
    compileOnly(libs.guava)
    compileOnly(libs.fastutil)
    testImplementation(libs.fastutil)

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
}
