/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.i18n

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.utils.capitalizeWords
import dev.kordex.core.utils.getKoin
import java.util.Locale

private fun getDefaultLocale(): Locale {
	val settings: ExtensibleBotBuilder = getKoin().get()

	return settings.i18nBuilder.defaultLocale
}

public fun Key.capitalizeWords(): Key = withPostProcessor {
	it.capitalizeWords(locale ?: getDefaultLocale())
}

public fun Key.capitalize(): Key = withPostProcessor { string ->
	string.replaceFirstChar {
		if (it.isLowerCase()) {
			it.titlecase(locale ?: getDefaultLocale())
		} else {
			it.toString()
		}
	}
}

public fun Key.lowercase(): Key = withPostProcessor {
	it.lowercase(locale ?: getDefaultLocale())
}

public fun Key.uppercase(): Key = withPostProcessor {
	it.uppercase(locale ?: getDefaultLocale())
}
