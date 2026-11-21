plugins {
    `java-library`
    `maven-publish`
}

// The coordinates of MyLib, under its own artifact id.
group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    // Provided by the server, like the other dependencies of MyLib.
    compileOnly("it.unimi.dsi:fastutil:8.5.16")
    compileOnly("org.jetbrains:annotations:26.0.1")

    // Testing
    testImplementation("it.unimi.dsi:fastutil:8.5.18")
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    javadoc {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).apply {
            links("https://docs.oracle.com/en/java/javase/21/docs/api/")
        }
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = "mylib-kv"
            version = project.version.toString()

            pom {
                name = "MyLib KV"
                description = "The disk-backed key-value store of MyLib, without Bukkit"
                url = "https://github.com/groundbreakingmc/MyLib"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "GroundbreakingMC"
                        name = "Victor"
                        url = "https://github.com/groundbreakingmc"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/groundbreakingmc/MyLib.git"
                    developerConnection = "scm:git:ssh://git@github.com:groundbreakingmc/MyLib.git"
                    url = "https://github.com/groundbreakingmc/MyLib"
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/groundbreakingmc/MyLib")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
