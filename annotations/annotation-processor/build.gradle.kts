plugins {
	`kordex-module`
	`published-module`
}

metadata {
	name = "KordEx: Annotation Processor"
	description = "KSP-based annotation processor designed for KordEx converters and plugins"
}

dependencies {
	implementation(libs.kotlin.stdlib)
	implementation(libs.kotlin.reflect)

	implementation(libs.koin.core)
	implementation(libs.ksp)

	implementation(project(":annotations:annotations"))

	detektPlugins(libs.detekt)
	detektPlugins(libs.detekt.libraries)
}

dokkaModule {
	moduleName.set("Kord Extensions: Annotation Processor")
}

java {
	sourceCompatibility = JavaVersion.VERSION_13
	targetCompatibility = JavaVersion.VERSION_13
}
