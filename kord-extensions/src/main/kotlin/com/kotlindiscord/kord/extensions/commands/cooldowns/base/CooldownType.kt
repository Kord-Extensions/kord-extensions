package com.kotlindiscord.kord.extensions.commands.cooldowns.base

import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent

/**
 * @suppress
 */
public interface CooldownType {
    /**
     * Returns a key that represents the cooldown type for a message command.
     *
     * @param event the event the command was sent from
     *
     * @return a key that represents this cooldown type, or null if a key could not be made
     */
    public suspend fun getCooldownKey(event: MessageCreateEvent): String?

    /**
     * Returns a key that represents the cooldown type for a slash command.
     *
     * @param event the event the slash command was sent from
     *
     * @return a key that represents this cooldown type, or null if a key could not be made
     */
    public suspend fun getSlashCooldownKey(event: InteractionCreateEvent): String?
}
