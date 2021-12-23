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

    kotlin("jvm") apply false

    id("com.github.jakemarsden.git-hooks")
}

val projectVersion: String by project

group = "com.kotlindiscord.kord.extensions"
version = projectVersion

val printVersion = task("printVersion") {
    print(version.toString())
}

gitHooks {
    setHooks(mapOf("pre-commit" to "licenseFormat detekt"))
}

repositories {
    google()
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
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.contracts.ExperimentalContracts"
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
