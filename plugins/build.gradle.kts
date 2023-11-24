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
	`tested-module`
}

metadata {
	name = "KordEx: Plugins"
	description = "Self-contained API implementing a simple plugin system"
}

group = "com.kotlindiscord.kord.extensions"

dependencies {
	detektPlugins(libs.detekt)
	detektPlugins(libs.detekt.libraries)

	implementation(libs.bundles.logging)
	implementation(libs.kotlin.stdlib)
	implementation(libs.kx.ser)
	implementation(libs.kx.ser.json)  // No ktor dep
	implementation(libs.semver)

	testImplementation(libs.groovy)  // For logback config
	testImplementation(libs.jansi)
	testImplementation(libs.junit)
	testImplementation(libs.logback)
	testImplementation(libs.logback.groovy)
}

dokkaModule {
	moduleName = "Kord Extensions: Plugin Framework"
}

tasks {
	dokkaHtmlMultiModule {
		enabled = false
	}
}
