package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.LinkedHashMap

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
