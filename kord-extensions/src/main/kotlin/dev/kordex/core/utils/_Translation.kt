/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils

import dev.kord.core.entity.interaction.GuildInteraction
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.ExtensibleBot
import java.util.*

internal val localeCache: WeakHashMap<Event, Locale> = WeakHashMap()

/** Attempt to resolve the locale for the given [MessageCreateEvent] object. **/
public suspend fun MessageCreateEvent.getLocale(): Locale {
	val existing = localeCache[this]

	if (existing != null) {
		return existing
	}

	val bot = getKoin().get<ExtensibleBot>()
	var result = bot.settings.i18nBuilder.defaultLocale

	for (resolver in bot.settings.i18nBuilder.localeResolvers) {
		val resolved = resolver(getGuildOrNull(), message.channel, message.author, null)

		if (resolved != null) {
			result = resolved
			break
		}
	}

	localeCache[this] = result

	return result
}

/** Attempt to resolve the locale for the given [InteractionCreateEvent] object. **/
public suspend fun InteractionCreateEvent.getLocale(): Locale {
	val existing = localeCache[this]

	if (existing != null) {
		return existing
	}

	val bot = getKoin().get<ExtensibleBot>()
	var result = bot.settings.i18nBuilder.defaultLocale

	for (resolver in bot.settings.i18nBuilder.localeResolvers) {
		val resolved = resolver(
			(interaction as? GuildInteraction)?.guild,
			interaction.channel,
			interaction.user,
			interaction
		)

		if (resolved != null) {
			result = resolved
			break
		}
	}

	localeCache[this] = result

	return result
}
