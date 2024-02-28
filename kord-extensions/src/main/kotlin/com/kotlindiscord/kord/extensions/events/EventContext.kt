/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events

import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.interactionFor
import com.kotlindiscord.kord.extensions.checks.userFor
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.sentry.SentryContext
import com.kotlindiscord.kord.extensions.types.TranslatableContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.event.Event
import java.util.*

/**
 * Light wrapper representing the context for an event handler's action.
 *
 * This is what `this` refers to in an event handler action body. You shouldn't need to instantiate this yourself.
 *
 * @param eventHandler Respective event handler for this context object.
 * @param event Event that triggered this event handler.
 * @param cache Data cache map shared with the defined checks.
 */
public open class EventContext<T : Event>(
	public open val eventHandler: EventHandler<T>,
	public open val event: T,
	public open val cache: MutableStringKeyedMap<Any>,
) : KordExKoinComponent, TranslatableContext {
	/** Translations provider, for retrieving translations. **/
	public val translationsProvider: TranslationsProvider by lazy { getTranslationProvider() }

	/** Current Sentry context, containing breadcrumbs and other goodies. **/
	public val sentry: SentryContext = SentryContext()

	override var resolvedLocale: Locale? = null

	override val bundle: String?
		get() = eventHandler.extension.bundle

	/**
	 * Given a translation key and optional bundle name, return the translation for the locale provided by the bot's
	 * configured locale resolvers.
	 */
	public override suspend fun translate(
		key: String,
		bundleName: String?,
		replacements: Array<Any?>,
	): String {
		val locale: Locale = getLocale()

		return translationsProvider.translate(key, locale, bundleName, replacements)
	}

	/**
	 * Given a translation key and optional bundle name, return the translation for the locale provided by the bot's
	 * configured locale resolvers.
	 */
	public override suspend fun translate(
		key: String,
		bundleName: String?,
		replacements: Map<String, Any?>,
	): String {
		val locale: Locale = getLocale()

		return translationsProvider.translate(key, locale, bundleName, replacements)
	}

	override suspend fun getLocale(): Locale {
		var locale = resolvedLocale

		if (locale != null) {
			return locale
		}

		val eventObj = event as Event

		val guild = guildFor(eventObj)
		val channel = channelFor(eventObj)
		val user = userFor(eventObj)

		for (resolver in eventHandler.extension.bot.settings.i18nBuilder.localeResolvers) {
			val result = resolver(guild, channel, user, interactionFor(eventObj))

			if (result != null) {
				locale = result

				break
			}
		}

		if (locale == null) {
			locale = eventHandler.extension.bot.settings.i18nBuilder.defaultLocale
		}

		resolvedLocale = locale

		return locale
	}
}
