package com.kotlindiscord.kord.extensions.commands.cooldowns.impl

import com.kotlindiscord.kord.extensions.commands.cooldowns.base.CooldownType
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent

/**
 * User Cooldown Type.
 * Key: U:{user_id}
 */
public class UserCooldown : CooldownType {
    override suspend fun getCooldownKey(event: MessageCreateEvent): String? =
        event.message.author?.id?.asString?.let { "U:$it" }

    override suspend fun getSlashCooldownKey(event: InteractionCreateEvent): String? =
        event.interaction.user.id.asString.let { "U:$it" }
}
