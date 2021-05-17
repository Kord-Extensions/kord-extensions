package com.kotlindiscord.kord.extensions.i18n

import java.util.*

/**
 * List of supported locales. These are locales with **merged** translations.
 *
 * If you've written a translation, don't try to modify this using reflection or something - instead, contribute it
 * back: https://crowdin.com/project/kordex
 */
public object SupportedLocales {
    public val ENGLISH: Locale = Locale("en", "gb")

    public val ALL_LOCALES: Map<String, Locale> = mapOf(
        "en" to ENGLISH,
    )
}
