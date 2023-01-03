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
    `kordex-module`
    `published-module`
    `dokka-module`
    `tested-module`
    `ksp-module`
}

dependencies {
    api(libs.h2)
    api(libs.hikari)
    api(libs.icu4j)  // For translations
    api(libs.koin.core)
    api(libs.koin.logger)

    api(libs.kord) {
        capabilities {
            requireCapability(libs.kord.voice.get().toString())
        }
    }

    api(libs.logging) // Basic logging setup
    api(libs.kx.ser)
    api(libs.sentry)  // Needs to be transitive or bots will start breaking
    api(libs.toml)
    api(libs.pf4j)

    api(project(":annotations"))
    api(project(":token-parser"))

    detektPlugins(libs.detekt)
    detektPlugins(libs.detekt.libraries)

    implementation(libs.bundles.commons)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.groovy)  // For logback config
    testImplementation(libs.jansi)
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.logback)

    ksp(project(":annotation-processor"))
    kspTest(project(":annotation-processor"))
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    languageVersion = "1.7"
}

dokkaModule {
    includes.add("packages.md")
}
