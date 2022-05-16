/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
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
public object T4JTimeUnitCache : KordExKoinComponent() {
    private val translations: TranslationsProvider by inject()
    private val valueCache: MutableMap<Locale, UnitMap> = mutableMapOf()

    /** Return a mapping of all translated unit names to ChronoUnit objects, based on the given locale. **/
    public fun getUnits(locale: Locale): UnitMap {
        if (valueCache[locale] == null) {
            val unitMap: UnitMap = linkedMapOf()

            keyMap.forEach { key, value ->
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
