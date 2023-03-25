/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.commands.converters.Converter
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider

/**
 * Data class representing a single argument.
 *
 * @param displayName Name shown on Discord in help messages, and used for keyword arguments.
 * @param description Short description explaining what the argument does.
 * @param converter Argument converter to use for this argument.
 */
public data class Argument<T : Any?>(
    val displayName: String,
    val description: String,
    val converter: Converter<T, *, *, *>,
) {
    init {
        converter.argumentObj = this
    }
}

internal fun Argument<*>.getDefaultTranslatedDisplayName(provider: TranslationsProvider, command: Command): String =
    provider.translate(displayName, provider.defaultLocale, command.resolvedBundle ?: converter.bundle)
