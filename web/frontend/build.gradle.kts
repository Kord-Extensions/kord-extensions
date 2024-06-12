import com.github.gradle.node.pnpm.task.PnpmTask

plugins {
	java

	id("com.github.node-gradle.node") version "7.0.1"
}

java {
	sourceCompatibility = JavaVersion.VERSION_13
	targetCompatibility = JavaVersion.VERSION_13
}

node {
	version = "20.11.0"
	download = true

	workDir = file("${project.projectDir}/.cache/nodejs")
	npmWorkDir = file("${project.projectDir}/.cache/npm")
	nodeProjectDir = file(project.projectDir)
}

val startTask = tasks.register<PnpmTask>("run") {
	dependsOn(tasks.pnpmInstall)

	args = listOf("run", "dev")
}

val lintTask = tasks.register<PnpmTask>("lintFrontend") {
	dependsOn(tasks.pnpmInstall)

	args = listOf("run", "lint")
}

val formatTask = tasks.register<PnpmTask>("formatFrontend") {
	dependsOn(tasks.pnpmInstall)

	args = listOf("run", "format")
}

val buildTask = tasks.register<PnpmTask>("buildFrontend") {
	dependsOn(tasks.pnpmInstall)

	inputs.dir("$projectDir/public")
	inputs.dir("$projectDir/src")
	inputs.file("$projectDir/components.json")
	inputs.file("$projectDir/index.html")
	inputs.file("$projectDir/package.json")
	inputs.file("$projectDir/pnpm-lock.yaml")
	inputs.file("$projectDir/postcss.config.cjs")
	inputs.file("$projectDir/tailwind.config.js")
	inputs.file("$projectDir/tsconfig.json")
	inputs.file("$projectDir/tsconfig.node.json")
	inputs.file("$projectDir/vite.config.ts")

	outputs.dir("$projectDir/dist")

	args = listOf("run", "build")
}

sourceSets {
	java {
		main {
			resources {
				srcDir(buildTask)
			}
		}
	}
}
