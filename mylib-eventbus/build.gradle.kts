plugins {
    id("mylib.module")
}

description = "The event bus of MyLib, without Bukkit"

dependencies {
    // Testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
