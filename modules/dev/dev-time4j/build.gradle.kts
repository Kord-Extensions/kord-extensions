plugins {
	`kordex-module`
	`published-module`
	`ksp-module`
}

group = "dev.kordex.modules"

metadata {
	name = "KordEx: Time4J"
	description = "KordEx module that provides converters that support Time4J"
}

dependencies {
	api(libs.time4j.base)
	api(libs.time4j.tzdata)

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
	moduleName.set("Kord Extensions: Time4J")
}
