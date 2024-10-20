/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events

import dev.kord.core.event.Event
import dev.kordex.core.checks.channelFor
import dev.kordex.core.checks.guildFor
import dev.kordex.core.checks.interactionFor
import dev.kordex.core.checks.userFor
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.sentry.SentryContext
import dev.kordex.core.types.TranslatableContext
import dev.kordex.core.utils.MutableStringKeyedMap
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
	/** Current Sentry context, containing breadcrumbs and other goodies. **/
	public val sentry: SentryContext = SentryContext()

	override var resolvedLocale: Locale? = null

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
