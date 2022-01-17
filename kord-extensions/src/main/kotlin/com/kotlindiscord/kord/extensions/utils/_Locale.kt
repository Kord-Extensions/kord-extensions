/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.Locale
import java.util.Locale as JLocale

/**
 * This converts the language codes used by Discord (e.g `de`) to the ones used by [JLocale] like `de_DE`.
 *
 * If [Locale.country] is already specified, it will just use the already specified version
 */
public fun Locale.convertToISO(): Locale = when {
    country != null -> this
    language == "cs" -> copy(country = "CZ")
    language == "da" -> copy(country = "DK")
    language == "el" -> copy(country = "GR")
    language == "hi" -> copy(country = "IN")
    language == "ja" -> copy(country = "JP")
    language == "uk" -> copy(country = "UA")
    language == "vi" -> copy(country = "VN")
    else -> Locale(language, language.uppercase(JLocale.ENGLISH))
}
