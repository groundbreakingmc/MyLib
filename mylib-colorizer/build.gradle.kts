plugins {
    id("mylib.module")
    id("mylib.jmh")
}

description = "The string and component colorizers of MyLib, without Bukkit"

dependencies {
    // Provided by the server: Paper and Velocity ship Adventure.
    compileOnly(libs.adventure.text.minimessage)
    compileOnly(libs.adventure.text.serializer.legacy)
    compileOnly(libs.adventure.text.serializer.plain)

    // Annotations
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.jetbrains.annotations)

    // Testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Benchmarking
    jmh(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.generator.annprocess)
}
