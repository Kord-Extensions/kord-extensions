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

    kotlin("jvm")

    id("com.github.jakemarsden.git-hooks")
}

val projectVersion: String by project

group = "com.kotlindiscord.kord.extensions"
version = projectVersion

val printVersion = task("printVersion") {
    print(version.toString())
}

gitHooks {
    setHooks(mapOf("pre-commit" to "updateLicenses detekt"))
}

repositories {
    google()
    mavenCentral()

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

subprojects {
    group = "com.kotlindiscord.kord.extensions"
    version = projectVersion

    tasks.withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.contracts.ExperimentalContracts"
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
