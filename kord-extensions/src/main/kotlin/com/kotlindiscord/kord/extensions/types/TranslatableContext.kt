/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.types

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExContext
import java.util.*

/**
 * Interface representing an execution context that supports translation convenience functions.
 *
 * As retrieving translations can very a lot based on the given context, several functions and properties must be
 * implemented by the extending type.
 */
public interface TranslatableContext {
	/** Cached locale variable, stored and retrieved by [getLocale]. **/
	public var resolvedLocale: Locale?

	/** Default bundle to use for the [translate] functions. **/
	public val bundle: String?

	/** Retrieve the bot's translation provider from Koin. **/
	public fun getTranslationProvider(): TranslationsProvider = KordExContext.get().get()

	/** Resolve the locale for this context, storing it in [resolvedLocale]. **/
	public suspend fun getLocale(): Locale

	/**
	 * Given a translation key and bundle name, return the translation for the locale provided by the bot's configured
	 * locale resolvers.
	 */
	public suspend fun translate(
		key: String,
		bundleName: String?,
		replacements: Array<Any?> = arrayOf(),
	): String

	/**
	 * Given a translation key and bundle name, return the translation for the locale provided by the bot's configured
	 * locale resolvers.
	 */
	public suspend fun translate(
		key: String,
		bundleName: String?,
		replacements: Map<String, Any?>,
	): String

	/**
	 * Given a translation key, return the translation for the locale provided by the bot's configured locale
	 * resolvers, using the bundle provided for this context.
	 */
	public suspend fun translate(
		key: String,
		replacements: Array<Any?> = arrayOf(),
	): String = translate(
		key, bundle, replacements
	)

	/**
	 * Given a translation key, return the translation for the locale provided by the bot's configured locale
	 * resolvers, using the bundle provided for this context.
	 */
	public suspend fun translate(
		key: String,
		replacements: Map<String, Any?>,
	): String = translate(
		key, bundle, replacements
	)
}
