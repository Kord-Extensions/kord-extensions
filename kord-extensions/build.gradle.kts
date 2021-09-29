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
    api(libs.icu4j)  // For translations
    api(libs.koin.core)
    api(libs.koin.logger)
    api(libs.kord)
    api(libs.logging) // Basic logging setup
    api(libs.kx.ser)
    api(libs.sentry)  // Needs to be transitive or bots will start breaking

    api(project(":token-parser"))

    detektPlugins(libs.detekt)

    implementation(libs.bundles.commons)
    implementation(libs.kotlin.stdlib)

    implementation(project(":annotations"))

    testImplementation(libs.groovy)  // For logback config
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.logback)

    ksp(project(":annotation-processor"))
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    languageVersion = "1.5"
}

dokkaModule {
    includes.add("packages.md")
}
