/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.time4j

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.koin.KordExKoinComponent
import net.time4j.CalendarUnit
import net.time4j.ClockUnit
import net.time4j.IsoUnit
import org.koin.core.component.inject
import java.util.*

private typealias UnitMap = LinkedHashMap<String, IsoUnit>

private val keyMap: UnitMap = linkedMapOf(
	"utils.units.second" to ClockUnit.SECONDS,
	"utils.units.minute" to ClockUnit.MINUTES,
	"utils.units.hour" to ClockUnit.HOURS,
	"utils.units.day" to CalendarUnit.DAYS,
	"utils.units.week" to CalendarUnit.WEEKS,
	"utils.units.month" to CalendarUnit.MONTHS,
	"utils.units.year" to CalendarUnit.YEARS,
)

/**
 * Simple object that caches translated time units per locale.
 */
public object T4JTimeUnitCache : KordExKoinComponent {
	private val translations: TranslationsProvider by inject()
	private val valueCache: MutableMap<Locale, UnitMap> = mutableMapOf()
	private val settings: ExtensibleBotBuilder by inject()

	init {
		settings.aboutBuilder.addCopyright()
	}

	/** Return a mapping of all translated unit names to ChronoUnit objects, based on the given locale. **/
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
