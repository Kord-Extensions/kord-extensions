/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.parsers.caches

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import kotlinx.datetime.DateTimeUnit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

private typealias UnitMap = LinkedHashMap<String, DateTimeUnit>

private val keyMap: UnitMap = linkedMapOf(
    "utils.units.second" to DateTimeUnit.SECOND,
    "utils.units.minute" to DateTimeUnit.MINUTE,
    "utils.units.hour" to DateTimeUnit.HOUR,
    "utils.units.day" to DateTimeUnit.DAY,
    "utils.units.week" to DateTimeUnit.WEEK,
    "utils.units.month" to DateTimeUnit.MONTH,
    "utils.units.year" to DateTimeUnit.YEAR,
)

/**
 * Simple object that caches translated time units per locale.
 */
public object TimeUnitCache : KoinComponent {
    private val translations: TranslationsProvider by inject()
    private val valueCache: MutableMap<Locale, UnitMap> = mutableMapOf()

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
