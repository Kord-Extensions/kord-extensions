plugins {
	`kordex-module`
	`published-module`
}

group = "dev.kordex.modules"

metadata {
	name = "KordEx Adapters: MongoDB"
	description = "KordEx data adapter for MongoDB, including extra codecs"
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

	implementation(libs.kotlin.stdlib)
	implementation(libs.kx.coro)
	implementation(libs.bundles.logging)
	implementation(libs.mongodb)
	implementation(libs.mongodb.bson.kotlinx)

	implementation(project(":kord-extensions"))
}

dokkaModule {
	moduleName = "Kord Extensions: MongoDB Data Adapter"
}
