/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.application.slash

import dev.kordex.core.InvalidCommandException
import dev.kordex.core.commands.application.Localized
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*

/**
 * Slash command group, containing other slash commands.
 *
 * @param name Slash command group name
 * @param parent Parent slash command that this group belongs to
 */
public class SlashGroup(
	public val name: Key,
	public val parent: SlashCommand<*, *, *>,
) : KordExKoinComponent {
	/** @suppress **/
	public val logger: KLogger = KotlinLogging.logger {}

	/** List of subcommands belonging to this group. **/
	public val subCommands: MutableList<SlashCommand<*, *, *>> = mutableListOf()

	/** Command group description, which is required and shown on Discord. **/
	public lateinit var description: Key

	/**
	 * A [Localized] version of [name].
	 */
	public val localizedName: Localized<String> by lazy {
		parent.localize(
			name,
			true
		)
	}

	/**
	 * A [Localized] version of [description].
	 */
	public val localizedDescription: Localized<String> by lazy {
		parent.localize(
			description
		)
	}

	/** Translation cache, so we don't have to look up translations every time. **/
	public val descriptionTranslationCache: MutableMap<Locale, String> = mutableMapOf()

	/** Return this group's description translated for the given locale, cached as required. **/
	public fun getTranslatedDescription(locale: Locale): String {
		// Only slash commands need this to be lower-cased.

		if (!descriptionTranslationCache.containsKey(locale)) {
			descriptionTranslationCache[locale] = description
				.withLocale(locale)
				.translate()
				.lowercase(locale)
		}

		return descriptionTranslationCache[locale]!!
	}

	/**
	 * Validate this command group, ensuring it has everything it needs.
	 *
	 * Throws if not.
	 */
	public fun validate() {
		if (!::description.isInitialized) {
			throw InvalidCommandException(name, "No group description given.")
		}

		if (subCommands.isEmpty()) {
			error("Command groups must contain at least one subcommand.")
		}
	}
}
