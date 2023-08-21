import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        maven {
            name = "Sonatype Snapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
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

metadata {
    name = "KordEx Core"
    description = "Core Kord Extensions module, providing everything you need to write a bot with KordEx"
}

dependencies {
    api(libs.icu4j)  // For translations
    api(libs.koin.core)
    api(libs.koin.logger)

    api(libs.kord)

    api(libs.bundles.logging) // Basic logging setup
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
    testImplementation(libs.logback.groovy)

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
