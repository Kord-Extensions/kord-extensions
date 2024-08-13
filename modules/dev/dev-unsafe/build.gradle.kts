plugins {
	`kordex-module`
	`published-module`
	`ksp-module`
}

group = "dev.kordex.modules"

metadata {
	name = "KordEx: Unsafe"
	description = "KordEx module that provides extra \"unsafe\" APIs that include lower-level functions and tools"
}

dependencies {
	implementation(libs.kotlin.stdlib)
	implementation(project(":kord-extensions"))

	detektPlugins(libs.detekt)
	detektPlugins(libs.detekt.libraries)

	testImplementation(libs.groovy)  // For logback config
	testImplementation(libs.junit)
	testImplementation(libs.logback)

	ksp(project(":annotations:annotation-processor"))
}

dokkaModule {
	moduleName.set("Kord Extensions: Unsafe Module")
}
