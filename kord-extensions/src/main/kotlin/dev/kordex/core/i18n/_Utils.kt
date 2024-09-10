/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.i18n

import dev.kordex.core.i18n.generated.CoreTranslations.bundle
import dev.kordex.core.i18n.types.Bundle
import dev.kordex.core.i18n.types.Key
import java.util.Locale

private val translationKeyMap: MutableMap<String, Key> = mutableMapOf()

public fun String.key(bundle: Bundle? = null, locale: Locale? = null): Key {
	var key = translationKeyMap.getOrPut(this) { Key(this) }

	if (bundle != null || locale != null) {
		key = key.withBoth(bundle, locale)
	}

	return key
}

public fun String.key(bundle: String, locale: Locale? = null): Key =
	key(Bundle(bundle), locale)

public fun String.key(locale: Locale): Key =
	translationKeyMap.getOrPut(this) { Key(this) }.withLocale(locale)

public fun String.key(bundle: Bundle): Key =
	translationKeyMap.getOrPut(this) { Key(this) }.withBundle(bundle)

public fun String.key(bundle: String): Key =
	key(Bundle(bundle))
