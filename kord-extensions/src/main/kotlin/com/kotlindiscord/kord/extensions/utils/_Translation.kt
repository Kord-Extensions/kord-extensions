/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
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
        val resolved = resolver(getGuild(), message.channel, message.author, null)

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
        val channel = interaction.channel.asChannel()

        val guild = if (channel is GuildChannel) {
            channel.guild
        } else {
            null
        }

        val resolved = resolver(guild, interaction.channel, interaction.user, interaction)

        if (resolved != null) {
            result = resolved
            break
        }
    }

    localeCache[this] = result

    return result
}
