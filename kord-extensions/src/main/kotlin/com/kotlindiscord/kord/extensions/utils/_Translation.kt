package com.kotlindiscord.kord.extensions.utils

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.core.event.message.MessageCreateEvent
import java.util.*

internal val localeCache: WeakHashMap<MessageCreateEvent, Locale> = WeakHashMap()

/** Attempt to resolve the locale for the given [MessageCreateEvent] object. **/
public suspend fun MessageCreateEvent.getLocale(): Locale {
    val existing = localeCache[this]

    if (existing != null) {
        return existing
    }

    val bot = getKoin().get<ExtensibleBot>()
    var result = bot.settings.i18nBuilder.defaultLocale

    for (resolver in bot.settings.i18nBuilder.localeResolvers) {
        val resolved = resolver(getGuild(), message.channel, message.author)

        if (resolved != null) {
            result = resolved
            break
        }
    }

    localeCache[this] = result

    return result
}
