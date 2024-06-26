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
	application

	`kordex-module`
	`ksp-module`
}

repositories {
	maven {
		name = "Sonatype Snapshots"
		url = uri("https://oss.sonatype.org/content/repositories/snapshots")
	}

	maven {
		name = "FabricMC"
		url = uri("https://maven.fabricmc.net/")
	}

	maven {
		name = "QuiltMC (Releases)"
		url = uri("https://maven.quiltmc.org/repository/release/")
	}

	maven {
		name = "QuiltMC (Snapshots)"
		url = uri("https://maven.quiltmc.org/repository/snapshot/")
	}

	maven {
		name = "Shedaniel"
		url = uri("https://maven.shedaniel.me")
	}

	maven {
		name = "JitPack"
		url = uri("https://jitpack.io")
	}
}

dependencies {
	implementation(project(":kord-extensions"))

	implementation(project(":extra-modules:extra-mappings"))
	implementation(project(":extra-modules:extra-phishing"))
	implementation(project(":extra-modules:extra-pluralkit"))

	implementation(project(":modules:java-time"))
	implementation(project(":modules:time4j"))
	implementation(project(":modules:unsafe"))

	implementation(project(":web:backend"))

	detektPlugins(libs.detekt)
	detektPlugins(libs.detekt.libraries)

	implementation(libs.bundles.commons)
	implementation(libs.kotlin.stdlib)

	implementation(libs.groovy)  // For logback config
	implementation(libs.jansi)
	implementation(libs.logback)
	implementation(libs.logback.groovy)

	ksp(project(":annotation-processor"))
	kspTest(project(":annotation-processor"))
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
	languageVersion = "1.7"
}

application {
	this.mainClass.set("com.kotlindiscord.kord.extensions.testbot.TestBotKt")
}

detekt {
	config.from(files("$projectDir/detekt.yml"))
}

dokkaModule {
	moduleName = "Kord Extensions: Test Bot"
}
