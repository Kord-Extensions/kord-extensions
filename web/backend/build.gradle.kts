plugins {
	`kordex-module`
	`published-module`
	`disable-explicit-api-mode`

	kotlin("plugin.serialization")
}

metadata {
	name = "KordEx Extra: Web"
	description = "KordEx extra module that provides a web interface and APIs for working with it"
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
}

group = "com.kotlindiscord.kord.extensions"

dokkaModule {
	moduleName = "Kord Extensions: Web Interface"
}
