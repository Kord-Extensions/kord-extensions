import org.gradle.api.publish.maven.MavenPublication

plugins {
    `maven-publish`
    signing
}

val sourceJar: Task by tasks.getting
val javadocJar: Task by tasks.getting

afterEvaluate {
    publishing {
        repositories {
            maven {
                name = "Sonatype"

                url = if (project.version.toString().contains("SNAPSHOT")) {
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                } else {
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                }

                credentials {
                    username = project.findProperty("ossrhUsername") as String?
                        ?: System.getenv("OSSRH_USERNAME")

                    password = project.findProperty("ossrhPassword") as String?
                        ?: System.getenv("OSSRH_PASSWORD")
                }

                version = project.version
            }
        }

        publications {
            create<MavenPublication>("maven") {
                from(components.getByName("java"))

                artifact(sourceJar)
                artifact(javadocJar)

                pom {
                    name.set(project.ext.get("pubName").toString())
                    description.set(project.ext.get("pubDesc").toString())

                    url.set("https://kordex.kotlindiscord.com")

                    packaging = "jar"

                    scm {
                        connection.set("scm:git:https://github.com/Kord-Extensions/kord-extensions.git")
                        developerConnection.set("scm:git:git@github.com:Kord-Extensions/kord-extensions.git")
                        url.set("https://github.com/Kord-Extensions/kord-extensions.git")
                    }

                    licenses {
                        license {
                            name.set("Mozilla Public License Version 2.0")
                            url.set("https://www.mozilla.org/en-US/MPL/2.0/")
                        }
                    }

                    developers {
                        developer {
                            id.set("gdude2002")
                            name.set("Gareth Coles")
                        }
                    }
                }
            }
        }
    }

    signing {
        val signingKey: String? by project ?: return@signing
        val signingPassword: String? by project ?: return@signing

        useInMemoryPgpKeys(signingKey, signingPassword)

        sign(publishing.publications["maven"])
    }
}
