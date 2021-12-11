plugins {
    `kotlin-dsl`
}

repositories {
    google()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.5.30"))
    implementation(kotlin("serialization", version = "1.5.30"))

    implementation("gradle.plugin.org.cadixdev.gradle", "licenser", "0.6.1")
    implementation("com.github.jakemarsden", "git-hooks-gradle-plugin", "0.0.2")
    implementation("com.google.devtools.ksp", "com.google.devtools.ksp.gradle.plugin", "1.5.30-1.0.0-beta08")
    implementation("io.gitlab.arturbosch.detekt", "detekt-gradle-plugin", "1.17.1")
    implementation("org.jetbrains.dokka", "dokka-gradle-plugin", "1.5.0")

    implementation(gradleApi())
    implementation(localGroovy())
}
