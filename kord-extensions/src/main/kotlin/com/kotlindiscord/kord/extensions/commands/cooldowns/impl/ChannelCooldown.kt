package com.kotlindiscord.kord.extensions.commands.cooldowns.impl

import com.kotlindiscord.kord.extensions.commands.cooldowns.base.CooldownType
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent

/**
 * Channel Cooldown Type.
 * Key: C:{channel_id}
 */
public class ChannelCooldown : CooldownType {
    override suspend fun getCooldownKey(event: MessageCreateEvent): String? =
        event.message.channelId.let { "C:$it" }

    override suspend fun getSlashCooldownKey(event: InteractionCreateEvent): String? =
        event.interaction.channelId.let { "C:$it" }
}
