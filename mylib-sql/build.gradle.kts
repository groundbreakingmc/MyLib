plugins {
    id("mylib.module")
}

description = "The SQL database layer of MyLib, without Bukkit"

dependencies {
    // Not bundled: the plugin that uses MyLib provides it, for example through plugin.yml libraries.
    compileOnly(libs.hikaricp)

    // Annotations
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.jetbrains.annotations)
}
