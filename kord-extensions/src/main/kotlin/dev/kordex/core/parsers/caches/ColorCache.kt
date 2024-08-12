/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.parsers.caches

import dev.kord.common.Color
import dev.kordex.core.*
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.koin.KordExKoinComponent
import org.koin.core.component.inject
import java.util.*

private typealias ColorMap = LinkedHashMap<String, Color>

private val keyMap: ColorMap = linkedMapOf(
	"utils.colors.black" to DISCORD_BLACK,
	"utils.colors.blurple" to DISCORD_BLURPLE,
	"utils.colors.fuchsia" to DISCORD_FUCHSIA,
	"utils.colors.green" to DISCORD_GREEN,
	"utils.colors.red" to DISCORD_RED,
	"utils.colors.white" to DISCORD_WHITE,
	"utils.colors.yellow" to DISCORD_YELLOW,
)

/** Simple object that caches translated colors per locale. **/
public object ColorCache : KordExKoinComponent {
	private val translations: TranslationsProvider by inject()
	private val valueCache: MutableMap<Locale, ColorMap> = mutableMapOf()

	/** Return a mapping of all translated colour names to Color objects, based on the given locale. **/
	public fun getColors(locale: Locale): ColorMap {
		if (valueCache[locale] == null) {
			val colorMap: ColorMap = linkedMapOf()

			keyMap.forEach { (key, value) ->
				val result = translations.translate(key, locale)

				result.split(",").map { it.trim() }.forEach {
					colorMap[it] = value
				}
			}

			valueCache[locale] = colorMap
		}

		return valueCache[locale]!!
	}
}
