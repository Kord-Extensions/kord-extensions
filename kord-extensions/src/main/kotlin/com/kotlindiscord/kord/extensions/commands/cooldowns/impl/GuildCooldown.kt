package com.kotlindiscord.kord.extensions.commands.cooldowns.impl

import com.kotlindiscord.kord.extensions.commands.cooldowns.CooldownType
import dev.kord.core.entity.interaction.GuildInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent

/**
 * Guild Cooldown Type.
 */
public class GuildCooldown : CooldownType() {
    override suspend fun getCooldownKey(event: MessageCreateEvent): String? =
        event.guildId?.asString?.let { "G:$it" }

    override suspend fun getSlashCooldownKey(event: InteractionCreateEvent): String? =
        (event as? GuildInteraction)?.guildId?.asString?.let { "G:$it" }
}
