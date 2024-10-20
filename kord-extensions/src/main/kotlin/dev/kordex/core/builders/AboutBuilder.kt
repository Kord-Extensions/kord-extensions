/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders

import dev.kordex.core.annotations.BotBuilderDSL
import dev.kordex.core.annotations.NotTranslated
import dev.kordex.core.builders.about.Copyright
import dev.kordex.core.builders.about.CopyrightType
import dev.kordex.core.builders.about.Section
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent

/**
 * Builder used for configuring the information provided by the "about" chat/slash command.
 */
@BotBuilderDSL
@Suppress("StringLiteralDuplication")
public class AboutBuilder : KordExKoinComponent {
	internal val copyrightItems: MutableList<Copyright> = mutableListOf()

	public var ephemeral: Boolean = true

	public val sections: MutableMap<Key, Section> = mutableMapOf()

	init {
		copyright("Kotlin", "Apache-2.0", CopyrightType.Tool, "https://kotlinlang.org")
		copyright("KSP", "Apache-2.0", CopyrightType.Tool, "https://kotlinlang.org/docs/ksp-overview.html")

		copyright(
			"Apache: Commons Validator",
			"Apache-2.0",
			CopyrightType.Library,
			"https://commons.apache.org/proper/commons-validator/"
		)

		copyright("ICU4J", "Unicode-3.0", CopyrightType.Library, "https://unicode-org.github.io/icu/userguide/icu4j/")
		copyright("JEmoji", "Apache-2.0", CopyrightType.Library, "https://github.com/felldo/JEmoji")
		copyright("Koin", "Apache-2.0", CopyrightType.Library, "https://insert-koin.io/")
		copyright("Kord", "MIT", CopyrightType.Library, "https://kord.dev/")
		copyright("KTor", "Apache-2.0", CopyrightType.Library, "https://ktor.io/")

		copyright(
			"Kotlinx: Coroutines",
			"Apache-2.0",
			CopyrightType.Library,
			"https://github.com/Kotlin/kotlinx.coroutines"
		)

		copyright(
			"Kotlinx: Serialization",
			"Apache-2.0",
			CopyrightType.Library,
			"https://github.com/Kotlin/kotlinx.serialization"
		)

		copyright("Kotlin Logging", "Apache-2.0", CopyrightType.Library, "https://github.com/oshai/kotlin-logging")

		copyright(
			"OSHI: Operating System and Hardware Information",
			"MIT",
			CopyrightType.Library,
			"https://www.oshi.ooo/"
		)

		copyright("PF4J: Plugin Framework for Java", "Apache-2.0", CopyrightType.Library, "https://pf4j.org/")
		copyright("TomlKT", "Apache-2.0", CopyrightType.Library, "https://github.com/Peanuuutz/tomlkt")
	}

	public fun copyright(name: String, license: String, type: CopyrightType, url: String? = null) {
		copyrightItems.add(
			Copyright(
				name = name,

				license = license,
				type = type,
				url = url,
			)
		)
	}

	public suspend fun general(builder: suspend Section.() -> Unit): Unit =
		section(
			CoreTranslations.Extensions.About.General.commandName,
			CoreTranslations.Extensions.About.General.commandDescription
		) {
			builder()
		}

	public suspend fun section(name: Key, description: Key, builder: suspend Section.() -> Unit) {
		val section = Section(name, description)

		builder(section)

		if (
			name.bundle == CoreTranslations.bundle &&
			name.key == CoreTranslations.Extensions.About.Copyright.commandName.key
		) {
			error("You may not replace the copyright section.")
		}

		sections[name] = section
	}

	@NotTranslated
	public suspend fun section(
		name: String,
		description: String,
		builder: suspend Section.() -> Unit
	) {
		val section = Section(name, description)

		builder(section)

		if (name == "extensions.about.copyright.commandName" || name == "copyright") {
			error("You may not replace the copyright section.")
		}

		sections[name.toKey()] = section
	}
}
