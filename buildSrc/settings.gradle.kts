// buildSrc is a build of its own, so it imports the version catalog of MyLib explicitly.
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
