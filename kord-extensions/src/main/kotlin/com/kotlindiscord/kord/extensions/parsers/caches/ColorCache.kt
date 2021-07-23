package com.kotlindiscord.kord.extensions.parsers.caches

import com.kotlindiscord.kord.extensions.*
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.Color
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

private typealias ColorMap = LinkedHashMap<String, Color>

private val keyMap: ColorMap = linkedMapOf(
    "utils.colors.black" to DISCORD_BLACK,
    "utils.colors.blurple" to DISCORD_BLURPLE,
    "utils.colors.fuchsia" to DISCORD_FUCHSIA,
    "utils.colors.green" to DISCORD_GREEN,
    "utils.colors.red" to DISCORD_RED,
    "utils.colors.white" to DISCORD_WHITE,
    "utils.colors.yellow" to DISCORD_YELLOW,
)

/** Simple object that caches translated colors per locale. **/
public object ColorCache : KoinComponent {
    private val translations: TranslationsProvider by inject()
    private val valueCache: MutableMap<Locale, ColorMap> = mutableMapOf()

    public fun getColors(locale: Locale): ColorMap {
        if (valueCache[locale] == null) {
            val colorMap: ColorMap = linkedMapOf()

            keyMap.forEach { key, value ->
                val result = translations.translate(key, locale)

                result.split(",").map { it.trim() }.forEach {
                    colorMap[it] = value
                }
            }

            valueCache[locale] = colorMap
        }

        return valueCache[locale]!!
    }
}
