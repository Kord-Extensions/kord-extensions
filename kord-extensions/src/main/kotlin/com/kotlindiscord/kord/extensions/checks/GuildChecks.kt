@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.event.Event
import mu.KotlinLogging

/**
 * Check asserting an [Event] was fired within a guild.
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 *
 * @param event Event object to check.
 */
public suspend fun anyGuild(event: Event): Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.anyGuild")

    return if (guildFor(event) != null) {
        logger.passed()

        true
    } else {
        logger.failed("Event did not happen within a guild.")

        false
    }
}

/**
 * Check asserting an [Event] was **not** fired within a guild.
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 *
 * @param event Event object to check.
 */
public suspend fun noGuild(event: Event): Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.noGuild")

    return if (guildFor(event) == null) {
        logger.passed()

        true
    } else {
        logger.failed("Event happened within a guild.")

        false
    }
}

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
