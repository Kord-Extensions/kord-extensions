plugins {
    `maven-publish`
    signing

    id("io.gitlab.arturbosch.detekt")

    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    maven {
        name = "KotDis"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

dependencies {
    detektPlugins(libs.detekt)

    implementation(libs.konf.core)
    implementation(libs.konf.toml)
    implementation(libs.logging)
    implementation(libs.kotlin.stdlib)

    implementation(project(":kord-extensions"))
}

/**
 * You probably don't want to touch anything below this line. It contains mostly boilerplate, and expands variables
 * from the gradle.properties file.
 */

val sourceJar = task("sourceJar", Jar::class) {
    dependsOn(tasks["classes"])
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

detekt {
    buildUponDefaultConfig = true
    config = rootProject.files("detekt.yml")

    autoCorrect = true
}

sourceSets {
    main {
        java {
            srcDir(file("$buildDir/generated/ksp/main/kotlin/"))
        }
    }

    test {
        java {
            srcDir(file("$buildDir/generated/ksp/test/kotlin/"))
        }
    }
}

tasks.build {
    this.finalizedBy(sourceJar)
}

publishing {
    repositories {
        maven {
            name = "KotDis"

            url = if (project.version.toString().contains("SNAPSHOT")) {
                uri("https://maven.kotlindiscord.com/repository/maven-snapshots/")
            } else {
                uri("https://maven.kotlindiscord.com/repository/maven-releases/")
            }

            credentials {
                username = project.findProperty("kotdis.user") as String? ?: System.getenv("KOTLIN_DISCORD_USER")
                password = project.findProperty("kotdis.password") as String?
                    ?: System.getenv("KOTLIN_DISCORD_PASSWORD")
            }

            version = project.version
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))

            artifact(sourceJar)
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}
