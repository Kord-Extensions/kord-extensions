plugins {
    `maven-publish`

    id("io.gitlab.arturbosch.detekt")

    kotlin("jvm")
}

repositories {
    maven {
        name = "KotDis"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }

    maven {
        name = "FabricMC"
        url = uri("https://maven.fabricmc.net/")
    }

    maven {
        name = "Bintray (Linkie)"
        url = uri("https://dl.bintray.com/shedaniel/linkie")
    }

    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // Linkie
    api("me.shedaniel:linkie-core:1.0.58")

    // Linting
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.15.0")

    // Configuration
    implementation("com.uchuhimo:konf:0.23.0")
    implementation("com.uchuhimo:konf-toml:0.23.0")

    // KordEx
    implementation(project(":kord-extensions"))

    // Kotlin libs
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
}

group = "com.kotlindiscord.kord.extensions"

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
