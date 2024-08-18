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

	implementation(project(":modules:dev:dev-java-time"))
	implementation(project(":modules:dev:dev-time4j"))
	implementation(project(":modules:dev:dev-unsafe"))
	implementation(project(":modules:functionality:func-mappings"))
	implementation(project(":modules:functionality:func-phishing"))
	implementation(project(":modules:integrations:pluralkit"))

	implementation(project(":modules:web:web-core:web-backend"))

	detektPlugins(libs.detekt)
	detektPlugins(libs.detekt.libraries)

	implementation(libs.bundles.commons)
	implementation(libs.kotlin.stdlib)

	implementation(libs.groovy)  // For logback config
	implementation(libs.jansi)
	implementation(libs.logback)
	implementation(libs.logback.groovy)

	ksp(project(":annotations:annotation-processor"))
	kspTest(project(":annotations:annotation-processor"))
}

application {
	this.mainClass.set("dev.kordex.test.bot.TestBotKt")
}

detekt {
	config.from(files("$projectDir/detekt.yml"))
}

dokkaModule {
	moduleName = "Kord Extensions: Test Bot"
}
