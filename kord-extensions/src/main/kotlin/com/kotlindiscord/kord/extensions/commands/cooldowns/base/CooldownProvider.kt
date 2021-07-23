package com.kotlindiscord.kord.extensions.commands.cooldowns.base

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * This keeps track of cooldowns for commands.
 */
@OptIn(ExperimentalTime::class)
public interface CooldownProvider {
    /**
     * Gets the cooldown time remaining for a command.
     *
     * @param key the key representing what type of cooldown it is
     */
    public fun getCooldown(key: String): Duration?

    /**
     * Returns a copy of all the cooldowns this object is storing.
     *
     * String - The Cooldown Key
     * Long - time in epochMillis of when this cooldown expires
     */
    public fun getCooldowns(): Map<String, Long>
}
