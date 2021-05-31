package com.kotlindiscord.kord.extensions.modules.extra.mappings

import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.guildFor
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.CategorizableChannel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.message.MessageCreateEvent
import mu.KotlinLogging

/**
 * Check ensuring that a message was sent within an allowed category, or not within a banned one.
 *
 * Passes when:
 * * Message doesn't happen in a channel
 * * Message happens in a channel that can't be in a category (eg, DMs)
 * * Message happens in an allowed category
 * * If there are no allowed categories, message doesn't happen in a banned category
 *
 * Fails when:
 * * Message happens outside of a category, when allowed categories are configured
 * * If there are allowed categories, message doesn't happen in an allowed category
 * * Message happens in a banned category
 *
 * @param allowed List of allowed category IDs
 * @param banned List of banned category IDs
 */
suspend fun allowedCategory(
    allowed: List<Snowflake>,
    banned: List<Snowflake>
): suspend (MessageCreateEvent) -> Boolean {
    val logger = KotlinLogging.logger { }

    suspend fun inner(event: MessageCreateEvent): Boolean {
        val channel = channelFor(event)

        if (channel == null) {
            logger.debug { "Passing: Event is not channel-related" }

            return true
        }

        if (channel !is CategorizableChannel) {
            logger.debug { "Passing: Channel is not categorizable (eg, it's a DM)" }

            return true  // It's a DM
        }

        val parent = channel.category

        if (allowed.isNotEmpty()) {
            if (parent == null) {
                logger.debug { "Failing: We have allowed categories, but the message was sent outside of a category" }

                return false
            }

            return if (allowed.contains(parent.id)) {
                logger.debug { "Passing: Event happened in an allowed category" }

                true
            } else {
                logger.debug { "Failing: Event happened outside of the allowed categories" }

                false
            }
        }

        if (parent == null) {
            logger.debug { "Passing: We have no allowed categories, and the message was sent outside of a category" }

            return true
        }

        if (banned.isNotEmpty()) {
            return if (!banned.contains(parent.id)) {
                logger.debug { "Passing: Event did not happen in a banned category" }

                true
            } else {
                logger.debug { "Failing: Event happened in a banned category" }

                false
            }
        }

        logger.debug { "Passing: No allowed or banned categories configured" }

        return true
    }

    return ::inner
}

/**
 * Check ensuring that a message was sent within an allowed channel, or not within a banned one.
 *
 * Passes when:
 * * Message doesn't happen in a channel
 * * Message happens in a channel that can't be in a guild (eg, DMs)
 * * Message happens in an allowed channel
 * * If there are no allowed channels, message doesn't happen in a banned channel
 *
 * Fails when:
 * * If there are allowed channels, message doesn't happen in an allowed channel
 * * Message happens in a banned channel
 *
 * @param allowed List of allowed channel IDs
 * @param banned List of banned channel IDs
 */
suspend fun allowedChannel(
    allowed: List<Snowflake>,
    banned: List<Snowflake>
): suspend (MessageCreateEvent) -> Boolean {
    val logger = KotlinLogging.logger { }

    suspend fun inner(event: MessageCreateEvent): Boolean {
        val channel = channelFor(event)

        if (channel == null) {
            logger.debug { "Passing: Event is not channel-related" }

            return true
        }

        if (channel !is GuildChannel) {
            logger.debug { "Passing: Message was sent privately" }

            return true  // It's a DM
        }

        if (allowed.isNotEmpty()) {
            return if (allowed.contains(channel.id)) {
                logger.debug { "Passing: Event happened in an allowed channel" }

                true
            } else {
                logger.debug { "Failing: Event did not happen in an allowed channel" }

                false
            }
        }

        if (banned.isNotEmpty()) {
            return if (!banned.contains(channel.id)) {
                logger.debug { "Passing: Event did not happen in a banned channel" }

                true
            } else {
                logger.debug { "Failing: Event happened in a banned channel" }

                false
            }
        }

        logger.debug { "Passing: No allowed or banned channels configured" }

        return true
    }

    return ::inner
}

/**
 * Check ensuring that a message was sent within an allowed guild, or not within a banned one.
 *
 * Passes when:
 * * Message doesn't happen in a guild
 * * Message happens in an allowed guild
 * * If there are no allowed guilds, message doesn't happen in a banned guild
 *
 * Fails when:
 * * If there are allowed guilds, message doesn't happen in an allowed guild
 * * Message happens in a banned guild
 *
 * @param allowed List of allowed guild IDs
 * @param banned List of banned guild IDs
 */
suspend fun allowedGuild(
    allowed: List<Snowflake>,
    banned: List<Snowflake>
): suspend (MessageCreateEvent) -> Boolean {
    val logger = KotlinLogging.logger { }

    suspend fun inner(event: MessageCreateEvent): Boolean {
        val guild = guildFor(event)

        if (guild == null) {
            logger.debug { "Passing: Event is not guild-related" }

            return true
        }

        if (allowed.isNotEmpty()) {
            return if (allowed.contains(guild.id)) {
                logger.debug { "Passing: Event happened in an allowed guild" }

                true
            } else {
                logger.debug { "Failing: Event did not happen in an allowed guild" }

                false
            }
        }

        if (banned.isNotEmpty()) {
            return if (!banned.contains(guild.id)) {
                logger.debug { "Passing: Event did not happen in a banned guild" }

                true
            } else {
                logger.debug { "Failing: Event happened in a banned guild" }

                false
            }
        }

        logger.debug { "Passing: No allowed or banned guilds configured" }

        return true
    }

    return ::inner
}
