@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import dev.kord.core.entity.Guild
import dev.kord.core.event.Event
import mu.KotlinLogging

/**
 * Check asserting that the guild an [Event] fired for is in a specific guild.
 *
 * Only events that can reasonably be associated with a guild are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param guild The guild to compare to.
 */
public fun inGuild(guild: Guild): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inGuild")

    suspend fun inner(event: Event): Boolean {
        val eventGuild = guildFor(event)

        if (eventGuild == null) {
            logger.nullGuild(event)
            return false
        }

        return if (eventGuild.id == guild.id) {
            logger.passed()
            true
        } else {
            logger.failed("Guild $eventGuild does not match $guild")
            false
        }
    }

    return ::inner
}

/**
 * Check asserting that the guild an [Event] fired for **is not** in a specific guild.
 *
 * Only events that can reasonably be associated with a guild are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param guild The guild to compare to.
 */
public fun notInGuild(guild: Guild): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInGuild")

    suspend fun inner(event: Event): Boolean {
        val eventGuild = guildFor(event)

        if (eventGuild == null) {
            logger.nullGuild(event)
            return false
        }

        return if (eventGuild.id != guild.id) {
            logger.passed()
            true
        } else {
            logger.failed("Guild $eventGuild matches $guild")
            false
        }
    }

    return ::inner
}
