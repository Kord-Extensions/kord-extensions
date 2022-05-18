/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.time.java

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import org.koin.core.component.inject
import java.time.temporal.ChronoUnit
import java.util.*

private typealias UnitMap = LinkedHashMap<String, ChronoUnit>

private val keyMap: UnitMap = linkedMapOf(
    "utils.units.second" to ChronoUnit.SECONDS,
    "utils.units.minute" to ChronoUnit.MINUTES,
    "utils.units.hour" to ChronoUnit.HOURS,
    "utils.units.day" to ChronoUnit.DAYS,
    "utils.units.week" to ChronoUnit.WEEKS,
    "utils.units.month" to ChronoUnit.MONTHS,
    "utils.units.year" to ChronoUnit.YEARS,
)

/**
 * Simple object that caches translated time units per locale.
 */
public object J8TimeUnitCache : KordExKoinComponent {
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
