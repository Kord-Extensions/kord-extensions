/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.optional
import dev.kord.core.behavior.interaction.ComponentInteractionBehavior
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.component.SelectOptionBuilder

/** Convenience wrapper for sending an ephemeral ack, optionally deferred, with less characters. **/
public suspend fun ComponentInteractionBehavior.ackEphemeral(
    deferred: Boolean = false
): EphemeralMessageInteractionResponseBehavior = if (deferred) {
    acknowledgeEphemeralDeferredMessageUpdate()
} else {
    acknowledgeEphemeral()
}

/** Convenience wrapper for sending a public ack, optionally deferred, with less characters. **/
public suspend fun ComponentInteractionBehavior.ackPublic(
    deferred: Boolean = false
): PublicMessageInteractionResponseBehavior = if (deferred) {
    acknowledgePublicDeferredMessageUpdate()
} else {
    acknowledgePublic()
}

/** Convenience function for setting [this.emoji] based on a given Unicode emoji. **/
public fun SelectOptionBuilder.emoji(unicodeEmoji: String) {
    this.emoji = DiscordPartialEmoji(
        name = unicodeEmoji
    )
}

/** Convenience function for setting [this.emoji] based on a given guild custom emoji. **/
public fun SelectOptionBuilder.emoji(guildEmoji: GuildEmoji) {
    this.emoji = DiscordPartialEmoji(
        id = guildEmoji.id,
        name = guildEmoji.name,
        animated = guildEmoji.isAnimated.optional()
    )
}

/** Convenience function for setting [this.emoji] based on a given reaction emoji. **/
public fun SelectOptionBuilder.emoji(unicodeEmoji: ReactionEmoji.Unicode) {
    this.emoji = DiscordPartialEmoji(
        name = unicodeEmoji.name
    )
}

/** Convenience function for setting [this.emoji] based on a given reaction emoji. **/
public fun SelectOptionBuilder.emoji(guildEmoji: ReactionEmoji.Custom) {
    this.emoji = DiscordPartialEmoji(
        id = guildEmoji.id,
        name = guildEmoji.name,
        animated = guildEmoji.isAnimated.optional()
    )
}

/** Convenience function for setting [this.emoji] based on a given reaction emoji. **/
public fun SelectOptionBuilder.emoji(emoji: ReactionEmoji): Unit = when (emoji) {
    is ReactionEmoji.Unicode -> emoji(emoji)
    is ReactionEmoji.Custom -> emoji(emoji)
}
