/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.i18n

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
	public abstract fun hasKey(key: String, locale: Locale, bundleName: String?): Boolean

	/** Get a translation by key from the given bundle name (`kordex.strings` by default). **/
	public open fun get(key: String, bundleName: String? = null): String =
		get(key, defaultLocale, bundleName)

	/** Get a translation by key from the given locale and bundle name (`kordex.strings` by default). **/
	public abstract fun get(key: String, locale: Locale, bundleName: String? = null): String

	/** Get a translation by key from the given language code and bundle name (`kordex.strings` by default). **/
	public open fun get(key: String, language: String, bundleName: String? = null): String =
		get(key, Locale(language), bundleName)

	/**
	 * Get a translation by key from the given language and country codes, and bundle name (`kordex.strings` by
	 * default).
	 */
	public open fun get(key: String, language: String, country: String, bundleName: String? = null): String =
		get(key, Locale(language, country), bundleName)

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		bundleName: String? = null,
		replacements: Array<Any?> = arrayOf(),
	): String = translate(key, defaultLocale, bundleName, replacements)

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		bundleName: String? = null,
		locale: Locale,
		replacements: Array<Any?> = arrayOf(),
	): String = translate(key, locale, bundleName, replacements)

	/** Get a formatted translation using the provided arguments. **/
	public abstract fun translate(
		key: String,
		locale: Locale,
		bundleName: String? = null,
		replacements: Array<Any?> = arrayOf(),
	): String

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		language: String,
		bundleName: String? = null,
		replacements: Array<Any?> = arrayOf(),
	): String = translate(key, Locale(language), bundleName, replacements)

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		language: String,
		country: String,
		bundleName: String? = null,
		replacements: Array<Any?> = arrayOf(),
	): String = translate(key, Locale(language, country), bundleName, replacements)

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		bundleName: String? = null,
		replacements: List<Any?>,
	): String = translate(key, bundleName, replacements.toTypedArray())

	/** Get a formatted translation using the provided arguments. **/
	public fun translate(
		key: String,
		locale: Locale,
		bundleName: String? = null,
		replacements: List<Any?>,
	): String = translate(key, locale, bundleName, replacements.toTypedArray())

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		language: String,
		bundleName: String? = null,
		replacements: List<Any?>,
	): String = translate(key, language, bundleName, replacements.toTypedArray())

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		language: String,
		country: String,
		bundleName: String? = null,
		replacements: List<Any?>,
	): String = translate(key, language, country, bundleName, replacements.toTypedArray())

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		bundleName: String? = null,
		replacements: Map<String, Any?>,
	): String = translate(key, defaultLocale, bundleName, replacements)

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		bundleName: String? = null,
		locale: Locale,
		replacements: Map<String, Any?>,
	): String = translate(key, locale, bundleName, replacements)

	/** Get a formatted translation using the provided arguments. **/
	public abstract fun translate(
		key: String,
		locale: Locale,
		bundleName: String? = null,
		replacements: Map<String, Any?>,
	): String

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		language: String,
		bundleName: String? = null,
		replacements: Map<String, Any?>,
	): String = translate(key, Locale(language), bundleName, replacements)

	/** Get a formatted translation using the provided arguments. **/
	public open fun translate(
		key: String,
		language: String,
		country: String,
		bundleName: String? = null,
		replacements: Map<String, Any?>,
	): String = translate(key, Locale(language, country), bundleName, replacements)
}
