// A published MyLib module. The artifact id is the module name, the POM description is the module description.
plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
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
        (options as StandardJavadocDocletOptions).links("https://docs.oracle.com/en/java/javase/21/docs/api/")
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = project.name
                description = provider { project.description }
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
