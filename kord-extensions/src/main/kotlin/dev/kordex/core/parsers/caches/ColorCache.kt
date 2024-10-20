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
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import java.util.*

private typealias ColorMap = LinkedHashMap<Key, Color>
private typealias StringColorMap = LinkedHashMap<String, Color>

private val keyMap: ColorMap = linkedMapOf(
	CoreTranslations.Utils.Colors.black to DISCORD_BLACK,
	CoreTranslations.Utils.Colors.blurple to DISCORD_BLURPLE,
	CoreTranslations.Utils.Colors.fuchsia to DISCORD_FUCHSIA,
	CoreTranslations.Utils.Colors.green to DISCORD_GREEN,
	CoreTranslations.Utils.Colors.red to DISCORD_RED,
	CoreTranslations.Utils.Colors.white to DISCORD_WHITE,
	CoreTranslations.Utils.Colors.yellow to DISCORD_YELLOW,
)

/** Simple object that caches translated colors per locale. **/
public object ColorCache : KordExKoinComponent {
	private val valueCache: MutableMap<Locale, StringColorMap> = mutableMapOf()

	/** Return a mapping of all translated colour names to Color objects, based on the given locale. **/
	public fun getColors(locale: Locale): StringColorMap {
		if (valueCache[locale] == null) {
			val colorMap: StringColorMap = linkedMapOf()

			keyMap.forEach { (key, value) ->
				val result = key
					.withLocale(locale)
					.translate()

				result.split(",").map { it.trim() }.forEach {
					colorMap[it] = value
				}
			}

			valueCache[locale] = colorMap
		}

		return valueCache[locale]!!
	}
}
