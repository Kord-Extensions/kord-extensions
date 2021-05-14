package com.kotlindiscord.kord.extensions.commands.cooldowns.impl

import com.kotlindiscord.kord.extensions.commands.cooldowns.Cooldown
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

/**
 * The default implementation for the cooldown object.
 */
@OptIn(ExperimentalTime::class)
public class CooldownImpl : Cooldown() {

    private val cooldownsMap: MutableMap<String, Instant> = mutableMapOf()
    private val slashCooldownsMap: MutableMap<String, Instant> = mutableMapOf()

    override fun setSlashCooldown(key: String, duration: Duration) {
        slashCooldownsMap[key] = Instant.now().plusSeconds(duration.toLong(DurationUnit.SECONDS))
    }

    override fun setCooldown(key: String, duration: Duration) {
        cooldownsMap[key] = Instant.now().plusSeconds(duration.toLong(DurationUnit.SECONDS))
    }

    override fun getCooldown(key: String): Duration? {
        val due = cooldownsMap[key] ?: return null
        val now = Instant.now()

        return if (due < now) {
            clearCooldown(key)
            null
        } else {
            (due.epochSecond - now.epochSecond).seconds
        }
    }

    override fun getSlashCooldown(key: String): Duration? {
        val due = cooldownsMap[key] ?: return null
        val now = Instant.now()

        return if (due < now) {
            clearCooldown(key)
            null
        } else {
            (due.epochSecond - now.epochSecond).seconds
        }
    }

    override fun clearCooldown(key: String) {
        cooldownsMap.remove(key)
    }

    override fun clearSlashCooldown(key: String) {
        slashCooldownsMap.remove(key)
    }
}
