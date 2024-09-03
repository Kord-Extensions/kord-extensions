import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")

	id("com.github.ben-manes.versions")
	id("dev.yumi.gradle.licenser")
	id("io.gitlab.arturbosch.detekt")

	id("org.jetbrains.dokka")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val dokkaModuleExtensionName = "dokkaModule"

abstract class DokkaModuleExtension {
	abstract val moduleName: Property<String>
	abstract val includes: ListProperty<String>
}

extensions.create<DokkaModuleExtension>(dokkaModuleExtensionName)

val sourceJar = task("sourceJar", Jar::class) {
	dependsOn(tasks["classes"])
	archiveClassifier = "sources"
	from(sourceSets.main.get().allSource)
}

val javadocJar = tasks.register<Jar>("javadocJar") {
	dependsOn(tasks.dokkaJavadoc)
	from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
	archiveClassifier = "javadoc"
}

//val dokkaJar = tasks.register<Jar>("dokkaJar") {
//	dependsOn(tasks.dokkaHtml)
//	from(tasks.dokkaHtml.flatMap { it.outputDirectory })
//	archiveClassifier = "html-docs"
//}

repositories {
	google()
	mavenCentral()

	maven {
		name = "KordEx (Releases)"
		url = uri("https://releases-repo.kordex.dev")
	}

	maven {
		name = "KordEx (Snapshots)"
		url = uri("https://snapshots-repo.kordex.dev")
	}

	maven {
		name = "Sonatype Snapshots"
		url = uri("https://oss.sonatype.org/content/repositories/snapshots")
	}
}

tasks {
	val projectDir = project.projectDir.relativeTo(rootProject.rootDir).toString()

	val propsTask = register<WriteProperties>("kordExProps") {
		group = "generation"
		description = "Generate KordEx properties file"

		comment = "Generated during KordEx compilation"
		destinationFile = layout.buildDirectory.file("kordex-build.properties")
		encoding = "UTF-8"

		property("git.branch", getCurrentGitBranch())
		property("git.hash", getCurrentGitHash())

		property("versions.kord", libs.findVersion("kord").get())
		property("versions.kordEx", project.version)
	}

	build {
		finalizedBy(sourceJar, javadocJar /*dokkaJar*/)
	}

	processResources {
		from(propsTask) {
			duplicatesStrategy = DuplicatesStrategy.INCLUDE
		}
	}

	kotlin {
		explicitApi()
	}

	jar {
		from(rootProject.file("build/LICENSE-kordex"))
	}

	afterEvaluate {
		rootProject.file("LICENSE").copyTo(rootProject.file("build/LICENSE-kordex"), true)

		tasks.withType<JavaCompile>().configureEach {
			sourceCompatibility = "13"
			targetCompatibility = "13"
		}

		tasks.withType<KotlinCompile>().configureEach {
			compilerOptions {
				freeCompilerArgs.add("-Xallow-kotlin-package")
				freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
				freeCompilerArgs.add("-opt-in=kotlin.contracts.ExperimentalContracts")

				jvmTarget = JvmTarget.JVM_13
			}
		}

		dokkaHtml {
			val extension = project.extensions.getByName<DokkaModuleExtension>(dokkaModuleExtensionName)

			extension.moduleName.orNull?.let {
				moduleName = it
			}

			dokkaSourceSets {
				configureEach {
					includeNonPublic = false
					skipDeprecated = false

					extension.moduleName.orNull?.let {
						displayName = it
					}

					extension.includes.orNull?.let {
						includes.from(*it.toTypedArray())
					}

					jdkVersion = 13

					sourceLink {
						localDirectory = file("${project.projectDir}/src/main/kotlin")

						remoteUrl = uri(
							"https://github.com/kord-extensions/kord-extensions/" +
								"tree/${getCurrentGitBranch()}/${projectDir}/src/main/kotlin"
						).toURL()

						remoteLineSuffix = "#L"
					}

					externalDocumentationLink {
						url = uri("https://dokka.kord.dev/").toURL()
					}
				}
			}
		}
	}
}

detekt {
	buildUponDefaultConfig = true
	config.from(rootProject.file("detekt.yml"))

	autoCorrect = true
}

license {
	rule(rootProject.file("codeformat/HEADER"))
}
