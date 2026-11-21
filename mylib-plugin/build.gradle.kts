plugins {
    java
    alias(libs.plugins.plugin.yml.bukkit)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":mylib-bukkit"))

    compileOnly(libs.paper.api)
    compileOnly(libs.jetbrains.annotations)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

base {
    // Named after the plugin: MyLib-<version>.jar.
    archivesName = "MyLib"
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    jar {
        // The plugin carries every MyLib module, so plugins on the server can use MyLib without shading it.
        dependsOn(configurations.runtimeClasspath)
        from({ configurations.runtimeClasspath.get().map(::zipTree) }) {
            exclude("META-INF/MANIFEST.MF")
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

    // The server downloads these, in the same versions MyLib compiles against.
    libraries = listOf(libs.caffeine, libs.configurate.yaml, libs.hikaricp, libs.typesafe.config).map {
        val library = it.get()
        "${library.module}:${library.versionConstraint.requiredVersion}"
    }
}
