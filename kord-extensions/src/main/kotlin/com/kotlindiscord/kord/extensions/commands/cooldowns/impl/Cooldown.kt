package com.kotlindiscord.kord.extensions.commands.cooldowns.impl

import com.kotlindiscord.kord.extensions.commands.cooldowns.base.CooldownProvider
import com.kotlindiscord.kord.extensions.commands.cooldowns.base.MutableCooldownProvider
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.*

/**
 * An implementation of [CooldownProvider] that will be used by default.
 */
@OptIn(ExperimentalTime::class)
public class Cooldown : MutableCooldownProvider {

    private val cooldownsMap: MutableMap<String, Long> = ConcurrentHashMap()

    override fun setCooldown(key: String, duration: Duration) {
        cooldownsMap[key] = System.currentTimeMillis() + duration.toLong(DurationUnit.MILLISECONDS)
    }

    override fun getCooldown(key: String): Duration? {
        val due = cooldownsMap[key] ?: return null
        val now = System.currentTimeMillis()

        return if (due < now) {
            clearCooldown(key)
            null
        } else {
            Duration.milliseconds(due - now)
        }
    }

    override fun clearCooldown(key: String) {
        cooldownsMap.remove(key)
    }

    override fun clearCooldowns() {
        val now = System.currentTimeMillis()
        for ((key, value) in cooldownsMap) {
            if (value < now) {
                clearCooldown(key)
            }
        }
    }

    override fun getCooldowns(): Map<String, Long> = cooldownsMap
}
