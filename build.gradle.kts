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
	`maven-publish`

	kotlin("jvm")

	id("com.github.jakemarsden.git-hooks")
	id("org.jetbrains.dokka")
}

val projectVersion: String by project

group = "dev.kordex"
version = projectVersion

val printVersion = task("printVersion") {
	doLast {
		print(version.toString())
	}
}

gitHooks {
	setHooks(mapOf("pre-commit" to "applyLicenses detekt"))
}

repositories {
	google()
	mavenCentral()

	maven {
		name = "Sonatype Snapshots"
		url = uri("https://oss.sonatype.org/content/repositories/snapshots")
	}
}

subprojects {
	group = "dev.kordex"
	version = projectVersion

	tasks.withType<KotlinCompile> {
		// Removing this block breaks the build, and I don't know why!
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
