plugins {
	`kordex-module`
	`published-module`
	`disable-explicit-api-mode`

	kotlin("plugin.serialization")
}

group = "dev.kordex.modules"

metadata {
	name = "KordEx Extra: Phishing"
	description = "KordEx extra module that provides anti-phishing functionality for bots"
}

getTranslations(
	"func-phishing",
	"dev.kordex.modules.func.phishing.i18n",
	"kordex.func-phishing",
	"PhishingTranslations"
)

repositories {
	maven {
		name = "Sonatype Snapshots"
		url = uri("https://oss.sonatype.org/content/repositories/snapshots")
	}
}

dependencies {
	detektPlugins(libs.detekt)
	detektPlugins(libs.detekt.libraries)

	implementation(libs.jsoup)

	implementation(libs.bundles.logging)
	implementation(libs.kotlin.stdlib)
	implementation(libs.ktor.logging)

	implementation(project(":kord-extensions"))
}

dokkaModule {
	moduleName = "Kord Extensions: Phishing Extension"
}
