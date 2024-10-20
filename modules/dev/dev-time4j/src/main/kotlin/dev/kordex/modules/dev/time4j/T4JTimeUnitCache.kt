/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.time4j

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import net.time4j.CalendarUnit
import net.time4j.ClockUnit
import net.time4j.IsoUnit
import org.koin.core.component.inject
import java.util.*

private typealias UnitMap = LinkedHashMap<Key, IsoUnit>
private typealias StringUnitMap = LinkedHashMap<String, IsoUnit>

private val keyMap: UnitMap = linkedMapOf(
	CoreTranslations.Utils.Units.second to ClockUnit.SECONDS,
	CoreTranslations.Utils.Units.minute to ClockUnit.MINUTES,
	CoreTranslations.Utils.Units.hour to ClockUnit.HOURS,
	CoreTranslations.Utils.Units.day to CalendarUnit.DAYS,
	CoreTranslations.Utils.Units.week to CalendarUnit.WEEKS,
	CoreTranslations.Utils.Units.month to CalendarUnit.MONTHS,
	CoreTranslations.Utils.Units.year to CalendarUnit.YEARS,
)

/**
 * Simple object that caches translated time units per locale.
 */
public object T4JTimeUnitCache : KordExKoinComponent {
	private val valueCache: MutableMap<Locale, StringUnitMap> = mutableMapOf()
	private val settings: ExtensibleBotBuilder by inject()

	init {
		settings.aboutBuilder.addCopyright()
	}

	/** Return a mapping of all translated unit names to ChronoUnit objects, based on the given locale. **/
	public fun getUnits(locale: Locale): StringUnitMap {
		if (valueCache[locale] == null) {
			val unitMap: StringUnitMap = linkedMapOf()

			keyMap.forEach { (key, value) ->
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
