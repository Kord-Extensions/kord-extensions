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
    public val PORTUGUESE: Locale = Locale("pt", "pt")

    public val ALL_LOCALES: Map<String, Locale> = mapOf(
        "中文" to CHINESE_SIMPLIFIED,
        "汉语" to CHINESE_SIMPLIFIED,
        "普通话" to CHINESE_SIMPLIFIED,
        "简体中文" to CHINESE_SIMPLIFIED,
        "chinese" to CHINESE_SIMPLIFIED,
        "zh" to CHINESE_SIMPLIFIED,
        "zh_cn" to CHINESE_SIMPLIFIED,

        "english" to ENGLISH,
        "en" to ENGLISH,
        "en_gb" to ENGLISH,
        "en_us" to ENGLISH,

        "suomen kieli" to FINNISH,
        "suomen" to FINNISH,
        "suomi" to FINNISH,
        "finnish" to FINNISH,
        "fi" to FINNISH,
        "fi_fi" to FINNISH,

        "français" to FRENCH,
        "francais" to FRENCH,
        "french" to FRENCH,
        "fr" to FRENCH,
        "fr_fr" to FRENCH,

        "deutsch" to GERMAN,
        "german" to GERMAN,
        "de" to GERMAN,
        "de_de" to GERMAN,

        "português" to PORTUGUESE,
        "portugues" to PORTUGUESE,
        "portuguese" to PORTUGUESE,
        "pt" to PORTUGUESE,
        "pt_pt" to PORTUGUESE,
    )
}
