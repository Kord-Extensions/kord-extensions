/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.parsers

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.parsers.caches.ColorCache
import dev.kord.common.Color
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
