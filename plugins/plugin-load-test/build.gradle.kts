buildscript {
	repositories {
		maven {
			name = "Sonatype Snapshots"
			url = uri("https://oss.sonatype.org/content/repositories/snapshots")
		}
	}
}

plugins {
	`kordex-module`
	`dokka-module`
	`tested-module`
}

group = "com.kotlindiscord.kord.extensions"

dependencies {
	detektPlugins(libs.detekt)
	detektPlugins(libs.detekt.libraries)

	testImplementation(libs.bundles.logging)
	testImplementation(libs.kotlin.stdlib)
	testImplementation(libs.kx.ser)
	testImplementation(libs.kx.ser.json)  // No ktor dep
	testImplementation(libs.semver)

	testImplementation(libs.groovy)  // For logback config
	testImplementation(libs.jansi)
	testImplementation(libs.junit)
	testImplementation(libs.logback)
	testImplementation(libs.logback.groovy)

	// Make sure these get built before the test module
	testImplementation(project(":plugins:test-plugin-core"))
	testImplementation(project(":plugins:test-plugin-1"))
	testImplementation(project(":plugins:test-plugin-2"))
}

val copyTestJars = tasks.register<Copy>("copyTestJars") {
	val matchRegex = Regex("^.*(\\d|-SNAPSHOT)\\.jar\$")

	val root = rootProject.rootDir
	val pluginDir = root.resolve("plugins/plugin-load-test/tmp/plugins")

	val testOneJar = root.resolve("plugins/test-plugin-1/build/libs")
		.listFiles()
		?.first {
			it.name.matches(matchRegex)
		}

	val testTwoJar = root.resolve("plugins/test-plugin-2/build/libs")
		.listFiles()
		?.first {
			it.name.matches(matchRegex)
		}

	pluginDir.mkdirs()

	from(testOneJar, testTwoJar)
	into(pluginDir)
}

tasks.test {
	dependsOn(copyTestJars)
}
