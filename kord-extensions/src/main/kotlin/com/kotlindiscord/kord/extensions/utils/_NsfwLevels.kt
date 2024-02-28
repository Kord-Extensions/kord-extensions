/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.entity.NsfwLevel
import java.util.*

/**
 * The way Discord assigned values for these makes me believe that they didn't intend for NSFW levels to be compared
 * based on severity. Nonetheless, bots will want this, so we supply sorting ordinals here..
 */
public val NsfwLevel.ordinal: Int
	get() = when (this) {
		NsfwLevel.Safe -> -1
		NsfwLevel.Default -> 0
		NsfwLevel.AgeRestricted -> 1
		NsfwLevel.Explicit -> 2

		is NsfwLevel.Unknown -> value
	}

/**
 * Use the corresponding [ordinal] value to compare two NSFW levels' severity.
 */
public operator fun NsfwLevel.compareTo(other: NsfwLevel): Int =
	ordinal.compareTo(other.ordinal)

/** Given a [NsfwLevel], return a string representing its translation key. **/
public fun NsfwLevel.toTranslationKey(): String? = when (this) {
	NsfwLevel.AgeRestricted -> "nsfwLevel.ageRestricted"
	NsfwLevel.Default -> "nsfwLevel.default"
	NsfwLevel.Explicit -> "nsfwLevel.explicit"
	NsfwLevel.Safe -> "nsfwLevel.safe"

	is NsfwLevel.Unknown -> null
}

/** Given a [CommandContext], translate the [NsfwLevel] to a human-readable string based on the context's locale. **/
public suspend fun NsfwLevel.translate(context: CommandContext): String {
	val key = toTranslationKey()

	return if (key == null) {
		context.translate(
			"nsfwLevel.unknown",
			replacements = arrayOf(value)
		)
	} else {
		context.translate(key)
	}
}

/** Given a locale, translate the [NsfwLevel] to a human-readable string. **/
public fun NsfwLevel.translate(locale: Locale): String {
	val key = toTranslationKey()

	return if (key == null) {
		getKoin().get<TranslationsProvider>().translate(
			"nsfwLevel.unknown",
			locale,
			replacements = arrayOf(value)
		)
	} else {
		getKoin().get<TranslationsProvider>().translate(key, locale)
	}
}
