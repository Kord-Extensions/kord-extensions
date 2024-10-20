import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.nio.file.Files
import java.util.Properties
import kotlin.collections.filterNotNull

public val KEYWORDS = arrayOf(
	"!in",
	"!is",
	"as",
	"as?",
	"break",
	"class",
	"continue",
	"do",
	"else",
	"false",
	"for",
	"fun",
	"if",
	"in",
	"interface",
	"is",
	"null",
	"object",
	"package",
	"return",
	"super",
	"this",
	"throw",
	"true",
	"try",
	"typealias",
	"typeof",
	"val",
	"var",
	"when",
	"while",
)

fun Project.getTranslations(classesPackage: String, bundle: String = project.name) {
	getTranslations(project.name, classesPackage, bundle)
}

fun Project.getTranslations(
	name: String,
	classesPackage: String,
	bundle: String = name,
	translationsClass: String = "Translations",
) {
	val outputDir = project.layout.buildDirectory.dir("translations")
	val gitDir = project.layout.buildDirectory.dir("translationsGit")

	val classOutputDir = project.layout.buildDirectory
		.dir("generated/kordex/main/kotlin")

	project.extensions.getByType<KotlinJvmProjectExtension>().sourceSets.getByName("main") {
		kotlin {
			srcDir(classOutputDir)
		}
	}

	val gitTask = tasks.create("getTranslations") {
		group = "generation"
		description = "Clone KordEx translations from Git."

		actions.add {
			if (!gitDir.get().asFile.exists()) {
				gitDir.get().asFile.mkdirs()

				runCommand(
					"git clone https://github.com/Kord-Extensions/translations.git translationsGit",
					project.layout.buildDirectory.get().asFile.path
				)
			} else {
				runCommand(
					"git pull",
					gitDir.get().asFile.path
				)
			}
		}
	}

	val copyTask = tasks.create<Sync>("copyTranslations") {
		group = "generation"
		description = "Copy correct module translations."

		from(gitDir.get().dir(name))
		into(outputDir.get().dir("translations/kordex"))

		dependsOn(gitTask)
	}

	val generateTask = tasks.create("generateKeysClass") {
		group = "generation"
		description = "Generate classes containing translation key references."

		dependsOn(copyTask)

		doLast {
			val props = Properties()

			val bundleName = if (bundle == name || "." in bundle) {
				bundle
			} else {
				"$name.$bundle"
			}

			props.load(
				Files.newBufferedReader(
					gitDir.get()
						.file("$name/${bundleName.split(".").last()}.properties")
						.asFile.toPath(),

					Charsets.UTF_8
				)
			)

			val keys = props.toList()
				.map { (left, _) -> left.toString() }

			createTranslationsClass("$classesPackage.generated", keys, props, bundleName, translationsClass)
				.writeTo(classOutputDir.get().asFile)
		}
	}

	tasks.getByName("build") {
		dependsOn(generateTask)
	}

	extensions
		.getByType<SourceSetContainer>()
		.first { it.name == "main" }
		.output.dir(
			mapOf("builtBy" to copyTask),
			outputDir
		)
}
