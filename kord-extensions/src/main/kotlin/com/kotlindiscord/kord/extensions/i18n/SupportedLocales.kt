/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.i18n

import java.util.*

/**
 * List of supported locales. These are locales with **merged** translations.
 *
 * If you've written a translation, don't try to modify this using reflection or something - instead, contribute it
 * back: https://hosted.weblate.org/projects/kord-extensions/main/
 */
public object SupportedLocales {
    /** Simplified Chinese locale. **/
    public val CHINESE_SIMPLIFIED: Locale = Locale("zh", "cn")

    /** English locale. **/
    public val ENGLISH: Locale = Locale("en", "gb")

    /** Finnish locale. **/
    public val FINNISH: Locale = Locale("fi", "fi")

    /** French locale. **/
    public val FRENCH: Locale = Locale("fr", "fr")

    /** German locale. **/
    public val GERMAN: Locale = Locale("de", "de")

    /** Polish locale. **/
    public val POLISH: Locale = Locale("pl", "pl")

    /** Portuguese locale. **/
    public val PORTUGUESE: Locale = Locale("pt", "pt")

    /** Russian locale. **/
    public val RUSSIAN: Locale = Locale("ru", "ru")

    /** Map of string names to supported Locale objects.. **/
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
