package com.kotlindiscord.kord.extensions.commands.cooldowns

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * @suppress
 */
@OptIn(ExperimentalTime::class)
public interface CooldownProvider {
    /**
     * Sets the cooldown for a command.
     *
     * @param key the key representing what type of cooldown it is
     * @param duration the duration for how long this cooldown will be
     */
    public fun setCooldown(key: String, duration: Duration)

    /**
     * Gets the cooldown time remaining for a message command.
     *
     * @param key the key representing what type of cooldown it is
     */
    public fun getCooldown(key: String): Duration?

    /**
     * Clears the cooldown time for a message command.
     *
     * @param key the key representing what type of cooldown it is
     */
    public fun clearCooldown(key: String)

    /**
     * Removes expired cooldowns for message commands.
     */
    public fun clearCooldowns()
}
