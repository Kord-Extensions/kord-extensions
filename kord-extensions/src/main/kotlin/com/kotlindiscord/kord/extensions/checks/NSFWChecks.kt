/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("RedundantSuspendModifier", "StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.utils.compareTo
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.NsfwLevel
import dev.kord.core.event.Event
import mu.KotlinLogging

/**
 * Check asserting an [Event] was fired within a guild with the given NSFW level.
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.hasGuildNsfwLevel(level: NsfwLevel) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.hasGuildNsfwLevel")
    val guild = guildFor(event)?.asGuildOrNull()

    if (guild == null) {
        logger.failed("Event did not happen within a guild.")

        fail(
            translate("checks.anyGuild.failed")
        )
    } else {
        if (guild.nsfw == level) {
            logger.passed()

            pass()
        } else {
            logger.failed("Guild did not have the correct NSFW level: $level")

            fail(
                translate(
                    "checks.guildNsfwLevelEqual.failed",

                    replacements = arrayOf(
                        level.translate(locale)
                    )
                )
            )
        }
    }
}

/**
 * Check asserting an [Event] was fired within a guild without the given NSFW level.
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.notHasGuildNsfwLevel(level: NsfwLevel) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notHasGuildNsfwLevel")
    val guild = guildFor(event)?.asGuildOrNull()

    if (guild == null) {
        logger.failed("Event did not happen within a guild.")

        fail(
            translate("checks.anyGuild.failed")
        )
    } else {
        if (guild.nsfw == level) {
            logger.failed("Guild matched the given NSFW level: $level")

            fail(
                translate(
                    "checks.guildNsfwLevelNotEqual.failed",

                    replacements = arrayOf(
                        level.translate(locale)
                    )
                )
            )
        } else {
            logger.passed()

            pass()
        }
    }
}

/**
 * Check asserting an [Event] was fired within a guild with a NSFW level higher than the provided one..
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.guildNsfwLevelHigher(level: NsfwLevel) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.guildNsfwLevelHigher")
    val guild = guildFor(event)?.asGuildOrNull()

    if (guild == null) {
        logger.failed("Event did not happen within a guild.")

        fail(
            translate("checks.anyGuild.failed")
        )
    } else {
        if (guild.nsfw > level) {
            logger.passed()

            pass()
        } else {
            logger.failed("Guild did not have a NSFW level higher than: $level")

            fail(
                translate(
                    "checks.guildNsfwLevelHigher.failed",

                    replacements = arrayOf(
                        level.translate(locale)
                    )
                )
            )
        }
    }
}

/**
 * Check asserting an [Event] was fired within a guild with a NSFW level higher than or equal to the provided one..
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.guildNsfwLevelHigherOrEqual(level: NsfwLevel) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.guildNsfwLevelHigherOrEqual")
    val guild = guildFor(event)?.asGuildOrNull()

    if (guild == null) {
        logger.failed("Event did not happen within a guild.")

        fail(
            translate("checks.anyGuild.failed")
        )
    } else {
        if (guild.nsfw >= level) {
            logger.passed()

            pass()
        } else {
            logger.failed("Guild did not have a NSFW level higher than or equal to: $level")

            fail(
                translate(
                    "checks.guildNsfwLevelHigherOrEqual.failed",

                    replacements = arrayOf(
                        level.translate(locale)
                    )
                )
            )
        }
    }
}

/**
 * Check asserting an [Event] was fired within a guild with a NSFW level lower than the provided one..
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.guildNsfwLevelLower(level: NsfwLevel) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.guildNsfwLevelLower")
    val guild = guildFor(event)?.asGuildOrNull()

    if (guild == null) {
        logger.failed("Event did not happen within a guild.")

        fail(
            translate("checks.anyGuild.failed")
        )
    } else {
        if (guild.nsfw < level) {
            logger.passed()

            pass()
        } else {
            logger.failed("Guild did not have a NSFW level lower than: $level")

            fail(
                translate(
                    "checks.guildNsfwLevelLower.failed",

                    replacements = arrayOf(
                        level.translate(locale)
                    )
                )
            )
        }
    }
}

/**
 * Check asserting an [Event] was fired within a guild with a NSFW level lower than or equal to the provided one..
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.guildNsfwLevelLowerOrEqual(level: NsfwLevel) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.guildNsfwLevelLowerOrEqual")
    val guild = guildFor(event)?.asGuildOrNull()

    if (guild == null) {
        logger.failed("Event did not happen within a guild.")

        fail(
            translate("checks.anyGuild.failed")
        )
    } else {
        if (guild.nsfw <= level) {
            logger.passed()

            pass()
        } else {
            logger.failed("Guild did not have a NSFW level lower than or equal to: $level")

            fail(
                translate(
                    "checks.guildNsfwLevelLowerOrEqual.failed",

                    replacements = arrayOf(
                        level.translate(locale)
                    )
                )
            )
        }
    }
}

/**
 * Check asserting that the channel an [Event] fired in is marked as NSFW.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 */
public suspend fun CheckContext<*>.channelIsNsfw() {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelIsNsfw")
    val eventChannel = channelFor(event)?.asChannel()

    if (eventChannel == null) {
        logger.nullChannel(event)

        fail()
    } else {
        if (eventChannel.data.nsfw.discordBoolean) {
            logger.passed()

            pass()
        } else {
            logger.failed("Channel is not marked as NSFW")

            fail(
                translate(
                    "checks.channelIsNsfw.failed"
                )
            )
        }
    }
}

/**
 * Check asserting that the channel an [Event] fired in is not marked as NSFW.
 *
 * DM channels can't be marked as NSFW, so this will always pass for a DM channel.
 */
public suspend fun CheckContext<*>.notChannelIsNsfw() {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notChannelIsNsfw")
    val eventChannel = channelFor(event)?.asChannel()

    if (eventChannel == null) {
        logger.nullChannel(event)

        fail()
    } else if (eventChannel.type == ChannelType.DM) {
        logger.passed()

        pass()
    } else {
        if (!eventChannel.data.nsfw.discordBoolean) {
            logger.passed()

            pass()
        } else {
            logger.failed("Channel is marked as NSFW")

            fail(
                translate(
                    "checks.notChannelIsNsfw.failed"
                )
            )
        }
    }
}

/**
 * Check asserting that the channel an [Event] fired in is marked as NSFW, or is in an NSFW guild.
 */
public suspend fun CheckContext<*>.channelOrGuildIsNsfw() {
    channelIsNsfw()
    or { guildNsfwLevelHigherOrEqual(NsfwLevel.AgeRestricted) }
}

/**
 * Check asserting that the channel an [Event] fired in is not marked as NSFW, and is not in an NSFW guild.
 */
public suspend fun CheckContext<*>.notChannelOrGuildIsNsfw() {
    notChannelIsNsfw()
    or { guildNsfwLevelLower(NsfwLevel.AgeRestricted) }
}
