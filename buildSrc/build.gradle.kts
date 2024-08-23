plugins {
	`kotlin-dsl`
}

repositories {
	google()
	gradlePluginPortal()
}

dependencies {
	implementation(kotlin("gradle-plugin", version = "2.0.20"))
	implementation(kotlin("serialization", version = "2.0.20"))

	implementation("com.github.ben-manes", "gradle-versions-plugin", "0.51.0")
	implementation("com.github.jakemarsden", "git-hooks-gradle-plugin", "0.0.2")
	implementation("com.google.devtools.ksp", "com.google.devtools.ksp.gradle.plugin", "2.0.20-1.0.24")
	implementation("dev.yumi", "yumi-gradle-licenser", "1.2.0")
	implementation("io.gitlab.arturbosch.detekt", "detekt-gradle-plugin", "1.23.6")
	implementation("org.jetbrains.dokka", "dokka-gradle-plugin", "1.9.20")

	implementation(gradleApi())
	implementation(localGroovy())
}
