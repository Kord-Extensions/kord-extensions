package com.kotlindiscord.kord.extensions.commands.cooldowns

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.core.Kord
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @suppress
 */
public abstract class CooldownType : KoinComponent {

    /** Current instance of the bot. **/
    public open val bot: ExtensibleBot by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public val kord: Kord by inject()

    /**
     * Returns a key that represents the cooldown type for a message command.
     *
     * @param event the event the command was sent from
     *
     * @return a key that represents this cooldown type, or null if a key could not be made
     */
    public abstract suspend fun getCooldownKey(event: MessageCreateEvent): String?

    /**
     * Returns a key that represents the cooldown type for a slash command.
     *
     * @param event the event the slash command was sent from
     *
     * @return a key that represents this cooldown type, or null if a key could not be made
     */
    public abstract suspend fun getSlashCooldownKey(event: InteractionCreateEvent): String?
}
