plugins {
    `kotlin-dsl`
}

repositories {
    google()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.9.22"))
    implementation(kotlin("serialization", version = "1.9.22"))

    implementation("gradle.plugin.org.cadixdev.gradle", "licenser", "0.6.1")
	implementation("com.github.ben-manes", "gradle-versions-plugin", "0.50.0")
    implementation("com.github.jakemarsden", "git-hooks-gradle-plugin", "0.0.2")
    implementation("com.google.devtools.ksp", "com.google.devtools.ksp.gradle.plugin", "1.9.22-1.0.16")
    implementation("io.gitlab.arturbosch.detekt", "detekt-gradle-plugin", "1.23.4")
    implementation("org.jetbrains.dokka", "dokka-gradle-plugin", "1.9.10")

    implementation(gradleApi())
    implementation(localGroovy())
}
