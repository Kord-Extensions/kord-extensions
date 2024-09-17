plugins {
	`kordex-module`
	`published-module`
	`disable-explicit-api-mode`

	kotlin("plugin.serialization")
}

group = "dev.kordex.modules"

metadata {
	name = "KordEx Extra: Welcome"
	description = "KordEx extra module that provides welcome channel management driven by web-accessible YAML files"
}

getTranslations(
	"func-welcome",
	"dev.kordex.modules.func.welcome.i18n",
	"kordex.func-welcome",
	"WelcomeTranslations"
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

	implementation(libs.bundles.logging)
	implementation(libs.kotlin.stdlib)
	implementation(libs.ktor.logging)

	implementation(libs.kaml)

	implementation(project(":kord-extensions"))
}

dokkaModule {
	moduleName = "Kord Extensions: Welcome Module"
}
