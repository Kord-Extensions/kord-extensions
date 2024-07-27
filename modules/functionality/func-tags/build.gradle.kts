plugins {
	`kordex-module`
	`published-module`
	`disable-explicit-api-mode`

	kotlin("plugin.serialization")
}

metadata {
	name = "KordEx Extra: Tags"
	description = "KordEx extra module that provides a set of commands for storing and retrieving tagged text snippets"
}

repositories {
	maven {
		name = "Sonatype Snapshots"
		url = uri("https://oss.sonatype.org/content/repositories/snapshots")
	}
}

dependencies {
	detektPlugins(libs.detekt)
	detektPlugins(libs.detekt.libraries)

	implementation(libs.bundles.logging)
	implementation(libs.kotlin.stdlib)
	implementation(libs.ktor.logging)

	implementation(project(":kord-extensions"))
	implementation(project(":modules:dev:dev-unsafe"))
}

group = "dev.kordex"

dokkaModule {
	moduleName = "Kord Extensions: Tags Module"
}
