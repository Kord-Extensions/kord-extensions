package com.kotlindiscord.kord.extensions.commands.cooldowns.base

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * This keeps track of cooldowns for commands, that can also be modified.
 */
@OptIn(ExperimentalTime::class)
public interface MutableCooldownProvider : CooldownProvider {
    /**
     * Sets the cooldown for a command.
     *
     * @param key the key representing what type of cooldown it is
     * @param duration the duration for how long this cooldown will be
     */
    public fun setCooldown(key: String, duration: Duration)

    /**
     * Clears the cooldown time for a command.
     *
     * @param key the key representing what type of cooldown it is
     */
    public fun clearCooldown(key: String)

    /**
     * Removes expired cooldowns for commands.
     */
    public fun clearCooldowns()
}
