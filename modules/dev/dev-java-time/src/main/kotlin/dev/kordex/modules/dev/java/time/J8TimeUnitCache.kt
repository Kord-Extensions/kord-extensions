/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.java.time

import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import java.time.temporal.ChronoUnit
import java.util.*

private typealias UnitMap = LinkedHashMap<Key, ChronoUnit>
private typealias StringUnitMap = LinkedHashMap<String, ChronoUnit>

private val keyMap: UnitMap = linkedMapOf(
	CoreTranslations.Utils.Units.second to ChronoUnit.SECONDS,
	CoreTranslations.Utils.Units.minute to ChronoUnit.MINUTES,
	CoreTranslations.Utils.Units.hour to ChronoUnit.HOURS,
	CoreTranslations.Utils.Units.day to ChronoUnit.DAYS,
	CoreTranslations.Utils.Units.week to ChronoUnit.WEEKS,
	CoreTranslations.Utils.Units.month to ChronoUnit.MONTHS,
	CoreTranslations.Utils.Units.year to ChronoUnit.YEARS,
)

/**
 * Simple object that caches translated time units per locale.
 */
public object J8TimeUnitCache : KordExKoinComponent {
	private val valueCache: MutableMap<Locale, StringUnitMap> = mutableMapOf()

	/** Return a mapping of all translated unit names to ChronoUnit objects, based on the given locale. **/
	public fun getUnits(locale: Locale): StringUnitMap {
		if (valueCache[locale] == null) {
			val unitMap: StringUnitMap = linkedMapOf()

			keyMap.forEach { key, value ->
				val result = key.translateLocale(locale)

				result.split(",").map { it.trim() }.forEach {
					unitMap[it] = value
				}
			}

			valueCache[locale] = unitMap
		}

		return valueCache[locale]!!
	}
}
