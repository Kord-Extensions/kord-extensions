import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        maven {
            name = "KotDis"
            url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
        }
    }
}

plugins {
    `maven-publish`

    kotlin("jvm") version "1.4.31"
    kotlin("plugin.serialization") version "1.4.31"

    id("io.gitlab.arturbosch.detekt") version "1.15.0"
    id("org.jetbrains.dokka") version "1.4.10.2"

    id("de.undercouch.download") version "4.1.1"
}

val projectVersion: String by project

group = "com.kotlindiscord.kord.extensions"
version = projectVersion

val printVersion = task("printVersion") {
    print(version.toString())
}

repositories {
    mavenCentral()

    maven {
        name = "KotDis"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

subprojects {
    group = "com.kotlindiscord.kord.extensions"
    version = projectVersion

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "9"

        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.contracts.ExperimentalContracts"

        kotlinOptions.useIR = true
    }

    repositories {
        rootProject.repositories.forEach {
            if (it is MavenArtifactRepository) {
                maven {
                    name = it.name
                    url = it.url
                }
            }
        }
    }
}
