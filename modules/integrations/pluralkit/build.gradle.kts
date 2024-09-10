plugins {
	`kordex-module`
	`published-module`
	`disable-explicit-api-mode`

	kotlin("plugin.serialization")
}

group = "dev.kordex.modules"

getTranslations(
	"pluralkit",
	"dev.kordex.modules.pluralkit.i18n",
	"kordex.pluralkit",
	"PluralKitTranslations"
)

metadata {
	name = "KordEx Extra: PluralKit"
	description = "KordEx extra module that provides PluralKit event functionality for bots"
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

dokkaModule {
	moduleName = "Kord Extensions: PluralKit Module"
}
