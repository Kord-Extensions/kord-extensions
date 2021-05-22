plugins {
    `maven-publish`

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
    // Linting
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.15.0")

    // Configuration
    implementation("com.uchuhimo:konf:0.23.0")
    implementation("com.uchuhimo:konf-toml:0.23.0")

    // KordEx
    implementation(project(":kord-extensions"))

    // Logging
    implementation("io.github.microutils:kotlin-logging:2.0.3")

    // Kotlin libs
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
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
