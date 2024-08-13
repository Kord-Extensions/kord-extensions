plugins {
	`kordex-module`
	`published-module`
	`ksp-module`

	kotlin("plugin.serialization")
}

group = "dev.kordex.modules"

metadata {
	name = "KordEx: Java Time"
	description = "KordEx module that provides converters that support Java Time"
}

dependencies {
	implementation(libs.kotlin.stdlib)

	implementation(project(":kord-extensions"))
	implementation(project(":annotations:annotations"))

	ksp(project(":annotations:annotation-processor"))

	detektPlugins(libs.detekt)
	detektPlugins(libs.detekt.libraries)

	testImplementation(libs.groovy)  // For logback config
	testImplementation(libs.jansi)
	testImplementation(libs.junit)
	testImplementation(libs.logback)
	testImplementation(libs.logback.groovy)
}

dokkaModule {
	moduleName.set("Kord Extensions: Java Time")
}

