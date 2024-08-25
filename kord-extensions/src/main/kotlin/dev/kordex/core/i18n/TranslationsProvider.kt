/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.i18n

import java.util.*

/**
 * Translation provider interface, in charge of taking string keys and returning translated strings.
 *
 * @param defaultLocaleBuilder Builder returning the default locale - available in [defaultLocale], which calls it
 * the first time it's accessed.
 */
public abstract class TranslationsProvider(
	public open val defaultLocaleBuilder: () -> Locale,
) {
	/**
	 * Default locale, resolved via [defaultLocaleBuilder]. Avoid accessing this outside of your [get] functions, as
	 * accessing it too early will prevent the user from configuring it properly.
	 */
	public open val defaultLocale: Locale by lazy { defaultLocaleBuilder() }

	/** Check whether a translation key exists in the given bundle and locale. **/
	public abstract fun hasKey(key: String, bundleName: String?, locale: Locale): Boolean

	/** Get a translation by key from the given locale and bundle name (`kordex.strings` by default). **/
	public abstract fun get(key: String, bundleName: String? = null, locale: Locale? = null): String

	/** Get a formatted translation using the provided arguments. **/
	public abstract fun translate(
		key: String,
		bundleName: String? = null,
		locale: Locale? = null,
		replacements: Array<Any?> = arrayOf(),
	): String

	/** Get a formatted translation using the provided arguments. **/
	public abstract fun translate(
		key: String,
		bundleName: String? = null,
		locale: Locale? = null,
		replacements: Map<String, Any?>,
	): String

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		locale: Locale? = null,
		replacements: Array<Any?> = arrayOf(),
	): String = translate(key = key, bundleName = null, locale = locale, replacements = replacements)

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		locale: Locale? = null,
		replacements: Map<String, Any?>,
	): String = translate(key = key, bundleName = null, locale = locale, replacements = replacements)
}
