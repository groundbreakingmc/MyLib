plugins {
    id("mylib.module")
}

description = "The disk-backed key-value store of MyLib, without Bukkit"

dependencies {
    // Provided by the server, like the other dependencies of MyLib.
    compileOnly(libs.fastutil)
    compileOnly(libs.jetbrains.annotations)

    // Testing
    testImplementation(libs.fastutil)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
