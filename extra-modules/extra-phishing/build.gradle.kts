plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
    `disable-explicit-api-mode`

    kotlin("plugin.serialization")
}

metadata {
    name = "KordEx Extra: Phishing"
    description = "KordEx extra module that provides anti-phishing functionality for bots"
}

repositories {
    maven {
        name = "KotDis"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

dependencies {
    detektPlugins(libs.detekt)
    detektPlugins(libs.detekt.libraries)

    implementation(libs.jsoup)

    implementation(libs.logging)
    implementation(libs.kotlin.stdlib)
    implementation(libs.ktor.logging)

    implementation(project(":kord-extensions"))
}

group = "com.kotlindiscord.kord.extensions"
