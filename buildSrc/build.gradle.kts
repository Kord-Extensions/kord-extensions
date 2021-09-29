plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.5.30"))
    implementation(kotlin("serialization", version = "1.5.30"))
    implementation("org.jetbrains.dokka", "dokka-gradle-plugin", "1.5.0")
    implementation("org.jetbrains.kotlinx", "atomicfu-gradle-plugin", "0.16.1")
    implementation("io.gitlab.arturbosch.detekt", "detekt-gradle-plugin", "1.18.1")
    implementation(gradleApi())
    implementation(localGroovy())
}
