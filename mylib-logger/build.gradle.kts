plugins {
    id("mylib.module")
}

description = "The console and file loggers of MyLib, without Bukkit"

dependencies {
    // Provided by the server: Paper and Velocity ship SLF4J.
    compileOnly(libs.slf4j.api)
    compileOnly(libs.jetbrains.annotations)
}
