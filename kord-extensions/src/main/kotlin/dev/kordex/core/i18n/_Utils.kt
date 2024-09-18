/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.i18n

import dev.kordex.core.i18n.types.Bundle
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.types.PlaceholderPosition
import java.util.Locale

private val translationKeyMap: MutableMap<String, Key> = mutableMapOf()

public fun String.toKey(
	bundle: Bundle? = null,
	locale: Locale? = null,
	presetPlaceholderPosition: PlaceholderPosition? = null,
	translateNestedKeys: Boolean? = null,
): Key {
	var key = translationKeyMap.getOrPut(this) { Key(this) }

	if (bundle != null || locale != null) {
		key = key.withBoth(bundle, locale)
	}

	if (presetPlaceholderPosition != null) {
		key = key.withPresetPlaceholderPosition(presetPlaceholderPosition)
	}

	if (translateNestedKeys != null) {
		key = key.withTranslateNestedKeys(translateNestedKeys)
	}

	return key
}

public fun String.toKey(bundle: String, locale: Locale? = null): Key =
	toKey(Bundle(bundle), locale)

public fun String.toKey(locale: Locale): Key =
	translationKeyMap.getOrPut(this) { Key(this) }.withLocale(locale)

public fun String.toKey(bundle: Bundle): Key =
	translationKeyMap.getOrPut(this) { Key(this) }.withBundle(bundle)

public fun String.toKey(bundle: String): Key =
	toKey(Bundle(bundle))
