/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.parsers.caches

import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import kotlinx.datetime.DateTimeUnit
import java.util.*

private typealias UnitMap = LinkedHashMap<Key, DateTimeUnit>
private typealias StringUnitMap = LinkedHashMap<String, DateTimeUnit>

/**
 * Simple object that caches translated time units per locale.
 */
public object TimeUnitCache : KordExKoinComponent {
	private val valueCache: MutableMap<Locale, StringUnitMap> = mutableMapOf()

	private val keyMap: UnitMap = linkedMapOf(
		CoreTranslations.Utils.Units.second to DateTimeUnit.SECOND,
		CoreTranslations.Utils.Units.minute to DateTimeUnit.MINUTE,
		CoreTranslations.Utils.Units.hour to DateTimeUnit.HOUR,
		CoreTranslations.Utils.Units.day to DateTimeUnit.DAY,
		CoreTranslations.Utils.Units.week to DateTimeUnit.WEEK,
		CoreTranslations.Utils.Units.month to DateTimeUnit.MONTH,
		CoreTranslations.Utils.Units.year to DateTimeUnit.YEAR,
	)

	/** Return a mapping of all translated unit names to DateTimeUnit objects, based on the given locale. **/
	public fun getUnits(locale: Locale): StringUnitMap {
		if (valueCache[locale] == null) {
			val unitMap: StringUnitMap = linkedMapOf()

			keyMap.forEach { (key, value) ->
				val result = key
					.withLocale(locale)
					.translate()

				result.split(",").map { it.trim() }.forEach {
					unitMap[it] = value
				}
			}

			valueCache[locale] = unitMap
		}

		return valueCache[locale]!!
	}
}
