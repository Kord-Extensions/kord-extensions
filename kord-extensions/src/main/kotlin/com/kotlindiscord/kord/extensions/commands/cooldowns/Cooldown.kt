package com.kotlindiscord.kord.extensions.commands.cooldowns

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.core.Kord
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * @suppress
 */
@OptIn(ExperimentalTime::class)
public abstract class Cooldown : KoinComponent {

    /** Current instance of the bot. **/
    public open val bot: ExtensibleBot by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public val kord: Kord by inject()

    /**
     * Sets the cooldown for a slash command.
     *
     * @param key the key representing what type of cooldown it is
     * @param duration the duration for how long this cooldown will be
     */
    public abstract fun setSlashCooldown(key: String, duration: Duration)

    /**
     * Sets the cooldown for a message command.
     *
     * @param key the key representing what type of cooldown it is
     * @param duration the duration for how long this cooldown will be
     */
    public abstract fun setCooldown(key: String, duration: Duration)

    /**
     * Gets the cooldown time remaining for a message command.
     *
     * @param key the key representing what type of cooldown it is
     */
    public abstract fun getCooldown(key: String): Duration?

    /**
     * Gets the cooldown time remaining for a slash command.
     *
     * @param key the key representing what type of cooldown it is
     */
    public abstract fun getSlashCooldown(key: String): Duration?

    /**
     * Clears the cooldown time for a message command.
     *
     * @param key the key representing what type of cooldown it is
     */
    public abstract fun clearCooldown(key: String)

    /**
     * Clears the cooldown time for a slash command.
     *
     * @param key the key representing what type of cooldown it is
     */
    public abstract fun clearSlashCooldown(key: String)

    /**
     * Removes expired cooldowns for message commands.
     */
    public abstract fun clearCooldowns()

    /**
     * Removes expired cooldowns for slash commands.
     */
    public abstract fun clearSlashCooldowns()
}
