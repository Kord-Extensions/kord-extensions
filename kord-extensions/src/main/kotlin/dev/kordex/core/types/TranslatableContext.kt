/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.types

import dev.kordex.core.i18n.types.Key
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

	/** Resolve the locale for this context, storing it in [resolvedLocale]. **/
	public suspend fun getLocale(): Locale

	/**
	 * Given a translation key, return the translation for the locale provided by the bot's configured
	 * locale resolvers.
	 */
	public suspend fun translate(
		key: Key,
		replacements: Array<Any?> = arrayOf(),
	): String

	/**
	 * Given a translation key, return the translation for the locale provided by the bot's configured
	 * locale resolvers.
	 */
	public suspend fun translate(
		key: Key,
		replacements: Map<String, Any?>,
	): String
}
