package com.kotlindiscord.kord.extensions.i18n

import java.util.*

/**
 * List of supported locales. These are locales with **merged** translations.
 *
 * If you've written a translation, don't try to modify this using reflection or something - instead, contribute it
 * back: https://hosted.weblate.org/projects/kord-extensions/main/
 */
public object SupportedLocales {
    public val CHINESE_SIMPLIFIED: Locale = Locale("zh", "cn")
    public val ENGLISH: Locale = Locale("en", "gb")
    public val FINNISH: Locale = Locale("fi", "fi")
    public val FRENCH: Locale = Locale("fr", "fr")
    public val GERMAN: Locale = Locale("de", "de")
    public val POLISH: Locale = Locale("pl", "pl")
    public val PORTUGUESE: Locale = Locale("pt", "pt")
    public val RUSSIAN: Locale = Locale("ru", "ru")

    public val ALL_LOCALES: Map<String, Locale> = mapOf(
        "chinese" to CHINESE_SIMPLIFIED,
        "zh" to CHINESE_SIMPLIFIED,
        "zh_cn" to CHINESE_SIMPLIFIED,
        "中文" to CHINESE_SIMPLIFIED,
        "普通话" to CHINESE_SIMPLIFIED,
        "汉语" to CHINESE_SIMPLIFIED,
        "简体中文" to CHINESE_SIMPLIFIED,

        "en" to ENGLISH,
        "en_gb" to ENGLISH,
        "en_us" to ENGLISH,
        "english" to ENGLISH,

        "fi" to FINNISH,
        "fi_fi" to FINNISH,
        "finnish" to FINNISH,
        "suomen kieli" to FINNISH,
        "suomen" to FINNISH,
        "suomi" to FINNISH,

        "fr" to FRENCH,
        "fr_fr" to FRENCH,
        "francais" to FRENCH,
        "français" to FRENCH,
        "french" to FRENCH,

        "de" to GERMAN,
        "de_de" to GERMAN,
        "deutsch" to GERMAN,
        "german" to GERMAN,

        "portugues" to PORTUGUESE,
        "portuguese" to PORTUGUESE,
        "português" to PORTUGUESE,
        "pt" to PORTUGUESE,
        "pt_pt" to PORTUGUESE,

        "pl" to POLISH,
        "pl_pl" to POLISH,
        "polish" to POLISH,
        "polska" to POLISH,
        "polskie" to POLISH,

        "ru" to RUSSIAN,
        "ru_ru" to RUSSIAN,
        "russian" to RUSSIAN,
        "русская" to RUSSIAN,
        "русскии" to RUSSIAN,
        "русский" to RUSSIAN,
    )
}
