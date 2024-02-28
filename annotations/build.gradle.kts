plugins {
	`kordex-module`
	`published-module`
}

metadata {
	name = "KordEx: Annotations"
	description = "Annotation definitions to be processed by the KordEx annotation processor"
}

dependencies {
	implementation(libs.kotlin.stdlib)

	detektPlugins(libs.detekt)
	detektPlugins(libs.detekt.libraries)
}

dokkaModule {
	moduleName.set("Kord Extensions: Annotations")
}
