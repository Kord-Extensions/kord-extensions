/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.parsers.caches

import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.koin.KordExKoinComponent
import kotlinx.datetime.DateTimeUnit
import org.koin.core.component.inject
import java.util.*

private typealias UnitMap = LinkedHashMap<String, DateTimeUnit>

/**
 * Simple object that caches translated time units per locale.
 */
public object TimeUnitCache : KordExKoinComponent {
	private val translations: TranslationsProvider by inject()
	private val valueCache: MutableMap<Locale, UnitMap> = mutableMapOf()

	private val keyMap: UnitMap = linkedMapOf(
		"utils.units.second" to DateTimeUnit.SECOND,
		"utils.units.minute" to DateTimeUnit.MINUTE,
		"utils.units.hour" to DateTimeUnit.HOUR,
		"utils.units.day" to DateTimeUnit.DAY,
		"utils.units.week" to DateTimeUnit.WEEK,
		"utils.units.month" to DateTimeUnit.MONTH,
		"utils.units.year" to DateTimeUnit.YEAR,
	)

	/** Return a mapping of all translated unit names to DateTimeUnit objects, based on the given locale. **/
	public fun getUnits(locale: Locale): UnitMap {
		if (valueCache[locale] == null) {
			val unitMap: UnitMap = linkedMapOf()

			keyMap.forEach { (key, value) ->
				val result = translations.translate(key, locale)

				result.split(",").map { it.trim() }.forEach {
					unitMap[it] = value
				}
			}

			valueCache[locale] = unitMap
		}

		return valueCache[locale]!!
	}
}
