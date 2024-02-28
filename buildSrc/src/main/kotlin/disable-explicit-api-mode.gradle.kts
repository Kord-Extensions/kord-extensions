import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
	kotlin("jvm")
}

kotlin {
	// https://github.com/JetBrains/kotlin/pull/4598
	fixExplicitApiModeArg()
	// We still need to set this, because the IntelliJ Kotlin plugin Inspections
	// look for this option instead of the CLI arg
	explicitApi = ExplicitApiMode.Disabled
}

fun fixExplicitApiModeArg() {
	val clazz = ExplicitApiMode.Disabled.javaClass
	val field = clazz.getDeclaredField("cliOption")

	with(field) {
		isAccessible = true
		set(ExplicitApiMode.Disabled, "disable")
	}
}
