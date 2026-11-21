plugins {
    id("mylib.module")
}

description = "The config wrapper of MyLib with Configurate and Lightbend backends, without Bukkit"

dependencies {
    // Backends: only the one a plugin picks has to be present.
    compileOnly(libs.configurate.yaml)
    compileOnly(libs.configurate.hocon)
    compileOnly(libs.typesafe.config)

    // Provided by the server, like the other dependencies of MyLib.
    compileOnly(libs.fastutil)
    compileOnly(libs.jetbrains.annotations)

    // Testing
    testImplementation(libs.fastutil)
    testCompileOnly(libs.jetbrains.annotations)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
