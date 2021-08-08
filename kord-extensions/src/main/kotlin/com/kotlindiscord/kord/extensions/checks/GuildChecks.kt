@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.Check
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.event.Event
import mu.KotlinLogging
import java.util.*

/**
 * Check asserting an [Event] was fired within a guild.
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public val anyGuild: Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.anyGuild")

    if (guildFor(event) != null) {
        logger.passed()

        pass()
    } else {
        logger.failed("Event did not happen within a guild.")

        fail(
            translate("checks.anyGuild.failed")
        )
    }
}

/**
 * Check asserting an [Event] was **not** fired within a guild.
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public val noGuild: Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.noGuild")

    if (guildFor(event) == null) {
        logger.passed()

        pass()
    } else {
        logger.failed("Event happened within a guild.")

        fail(
            translate("checks.noGuild.failed")
        )
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
public fun inGuild(builder: suspend () -> GuildBehavior): Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inGuild")
    val eventGuild = guildFor(event)?.asGuildOrNull()

    if (eventGuild == null) {
        logger.nullGuild(event)

        fail()
    } else {
        val guild = builder()

        if (eventGuild.id == guild.id) {
            logger.passed()

            pass()
        } else {
            logger.failed("Guild $eventGuild does not match $guild")

            fail(
                translate(
                    "checks.inGuild.failed",
                    replacements = arrayOf(eventGuild.name),
                )
            )
        }
    }
}

/**
 * Check asserting that the guild an [Event] fired for **is not** in a specific guild.
 *
 * Only events that can reasonably be associated with a guild are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the guild to compare to.
 */
public fun notInGuild(builder: suspend () -> GuildBehavior): Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInGuild")
    val eventGuild = guildFor(event)?.asGuild()

    if (eventGuild == null) {
        logger.nullGuild(event)

        pass()
    } else {
        val guild = builder()

        if (eventGuild.id != guild.id) {
            logger.passed()

            pass()
        } else {
            logger.failed("Guild $eventGuild matches $guild")

            fail(
                translate(
                    "checks.notInGuild.failed",
                    replacements = arrayOf(eventGuild.name),
                )
            )
        }
    }
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
public fun inGuild(id: Snowflake): Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inGuild")
    val guild = event.kord.getGuild(id)

    if (guild == null) {
        logger.noGuildId(id)

        fail()
    } else {
        inGuild { guild }()
    }
}

/**
 * Check asserting that the guild an [Event] fired for **is not** in a specific guild.
 *
 * Only events that can reasonably be associated with a guild are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Guild snowflake to compare to.
 */
public fun notInGuild(id: Snowflake): Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInGuild")
    val guild = event.kord.getGuild(id)

    if (guild == null) {
        logger.noGuildId(id)

        pass()
    } else {
        notInGuild { guild }()
    }
}

// endregion
