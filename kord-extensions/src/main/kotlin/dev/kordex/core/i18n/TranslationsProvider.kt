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
import dev.kordex.core.koin.KordExKoinComponent
import java.util.*

/**
 * Translation provider interface, in charge of taking string keys and returning translated strings.
 *
 * @param defaultLocaleBuilder Builder returning the default locale - available in [defaultLocale], which calls it
 * the first time it's accessed.
 */
public abstract class TranslationsProvider(
	public open val defaultLocaleBuilder: () -> Locale,
) : KordExKoinComponent {
	/**
	 * Default locale, resolved via [defaultLocaleBuilder]. Avoid accessing this outside of your [get] functions, as
	 * accessing it too early will prevent the user from configuring it properly.
	 */
	public open val defaultLocale: Locale by lazy { defaultLocaleBuilder() }

	/** Check whether a translation key exists. **/
	public abstract fun hasKey(key: Key): Boolean

	/** Get a translation by key. **/
	public abstract fun get(key: Key): String

	/** Get a formatted translation using the provided arguments. **/
	@Deprecated(
		"Manual translation API access is unsupported. Create [Key] objects and use them instead.",
		level = DeprecationLevel.WARNING,
	)
	public fun translate(
		key: String,
		bundleName: String? = null,
		locale: Locale? = null,
		replacements: Array<Any?> = arrayOf(),
	): String = translate(Key(key, bundleName?.let { Bundle(it) }, locale), replacements)

	/** Get a formatted translation using the provided arguments. **/
	@Deprecated(
		"Manual translation API access is unsupported. Create [Key] objects and use them instead.",
		level = DeprecationLevel.WARNING,
	)
	public fun translate(
		key: String,
		bundleName: String? = null,
		locale: Locale? = null,
		replacements: Map<String, Any?>,
	): String = translateNamed(Key(key, bundleName?.let { Bundle(it) }, locale), replacements)

	/** Get a formatted translation using the provided arguments. **/
	public abstract fun translate(
		key: Key,
		replacements: Array<Any?> = arrayOf(),
	): String

	/** Get a formatted translation using the provided arguments. **/
	public abstract fun translateNamed(
		key: Key,
		replacements: Map<String, Any?>,
	): String
}
