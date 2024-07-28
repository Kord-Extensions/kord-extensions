import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

fun Project.getTranslations() {
	getTranslations(project.name)
}

fun Project.getTranslations(name: String) {
	val outputDir = project.layout.buildDirectory.dir("translations")
	val gitDir = project.layout.buildDirectory.dir("translationsGit")

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

	tasks.getByName("build") {
		dependsOn(copyTask)
	}

	extensions
		.getByType<SourceSetContainer>()
		.first { it.name == "main" }
		.output.dir(
			mapOf("builtBy" to copyTask),
			outputDir
		)
}
