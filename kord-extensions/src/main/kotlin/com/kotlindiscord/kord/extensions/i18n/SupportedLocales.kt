package com.kotlindiscord.kord.extensions.i18n

import java.util.*

/**
 * List of supported locales. These are locales with **merged** translations.
 *
 * If you've written a translation, don't try to modify this using reflection or something - instead, contribute it
 * back: https://crowdin.com/project/kordex
 */
public object SupportedLocales {
    public val CHINESE_SIMPLIFIED: Locale = Locale("zh", "cn")
    public val ENGLISH: Locale = Locale("en", "gb")
    public val FINNISH: Locale = Locale("fi", "fi")
    public val FRENCH: Locale = Locale("fr", "fr")
    public val GERMAN: Locale = Locale("de", "de")

    public val ALL_LOCALES: Map<String, Locale> = mapOf(
        "zh" to CHINESE_SIMPLIFIED,
        "en" to ENGLISH,
        "fi" to FINNISH,
        "fr" to FRENCH,
        "de" to GERMAN,
    )
}
