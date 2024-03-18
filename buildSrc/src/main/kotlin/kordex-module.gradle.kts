import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")

	id("com.github.ben-manes.versions")
	id("io.gitlab.arturbosch.detekt")
	id("org.cadixdev.licenser")

	id("org.jetbrains.dokka")
}

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

tasks {
	val projectDir = project.projectDir.relativeTo(rootProject.rootDir).toString()

	build {
		finalizedBy(sourceJar, javadocJar /*dokkaJar*/)
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

		withType<KotlinCompile>().configureEach {
			compilerOptions {
				freeCompilerArgs.add("-Xallow-kotlin-package")
			}

			kotlinOptions {
				jvmTarget = "13"
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
	config.from(files("$rootDir/detekt.yml"))

	autoCorrect = true
}

license {
	setHeader(rootProject.file("codeformat/HEADER"))
	ignoreFailures(System.getenv()["CI"] == null)

	include("**/src/**.*")
	include("src/**.*")
}
