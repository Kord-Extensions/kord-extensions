@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.event.Event
import mu.KotlinLogging

// region: Entity DSL versions

/**
 * Check asserting that the guild an [Event] fired for is in a specific guild.
 *
 * Only events that can reasonably be associated with a guild are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the guild to compare to.
 */
public fun inGuild(builder: suspend () -> GuildBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inGuild")

    suspend fun inner(event: Event): Boolean {
        val eventGuild = guildFor(event)

        if (eventGuild == null) {
            logger.nullGuild(event)
            return false
        }

        val guild = builder()

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
 * @param builder Lambda returning the guild to compare to.
 */
public fun notInGuild(builder: suspend () -> GuildBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInGuild")

    suspend fun inner(event: Event): Boolean {
        val eventGuild = guildFor(event)

        if (eventGuild == null) {
            logger.nullGuild(event)
            return false
        }

        val guild = builder()

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

// endregion

// region: Snowflake versions

/**
 * Check asserting that the guild an [Event] fired for is in a specific guild.
 *
 * Only events that can reasonably be associated with a guild are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Guild snowflake to compare to.
 */
public fun inGuild(id: Snowflake): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inGuild")

    suspend fun inner(event: Event): Boolean {
        val guild = event.kord.getGuild(id)

        if (guild == null) {
            logger.noGuildId(id)
            return false
        }

        return inGuild { guild }(event)
    }

    return ::inner
}

/**
 * Check asserting that the guild an [Event] fired for **is not** in a specific guild.
 *
 * Only events that can reasonably be associated with a guild are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Guild snowflake to compare to.
 */
public fun notInGuild(id: Snowflake): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInGuild")

    suspend fun inner(event: Event): Boolean {
        val guild = event.kord.getGuild(id)

        if (guild == null) {
            logger.noGuildId(id)
            return false
        }

        return notInGuild { guild }(event)
    }

    return ::inner
}

// endregion
