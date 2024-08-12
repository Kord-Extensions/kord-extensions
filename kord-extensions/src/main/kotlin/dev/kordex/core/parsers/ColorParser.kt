/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.parsers

import dev.kord.common.Color
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.parsers.caches.ColorCache
import org.koin.core.component.inject
import java.util.*

/**
 * Object in charge of parsing strings into [Color]s using terms defined in the given locale's translations, falling
 * back to the bot's default locale if the input doesn't match.
 *
 * The relevant translations keys are:
 *
 * * `utils.colors.black`
 * * `utils.colors.blurple` (which should include purple)
 * * `utils.colors.fuchsia` (which should include pink)
 * * `utils.colors.green`
 * * `utils.colors.red`
 * * `utils.colors.white`
 * * `utils.colors.yellow`
 *
 * Translations may be split using commas, in which case any of the given values will be suitable.
 */
public object ColorParser : KordExKoinComponent {
	private val settings: ExtensibleBotBuilder by inject()

	/**
	 * Parse the given string into a [Color] based on the translations for the given locale. Falls back to the bot's
	 * default locale as required.
	 */
	public fun parse(input: String, locale: Locale): Color? {
		val defaultColorMap = ColorCache.getColors(settings.i18nBuilder.defaultLocale)
		val colorMap = ColorCache.getColors(locale)

		return colorMap[input] ?: defaultColorMap[input]
	}
}
