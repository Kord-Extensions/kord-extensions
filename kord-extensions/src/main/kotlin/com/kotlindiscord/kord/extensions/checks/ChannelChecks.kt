@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import dev.kord.core.behavior.channel.CategoryBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.event.Event
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

/**
 * Check asserting that an [Event] fired within a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channel The channel to compare to.
 */
public fun inChannel(channel: ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inChannel")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        return if (eventChannel.id == channel.id) {
            logger.passed()
            true
        } else {
            logger.failed("Channel $eventChannel is not the same as channel $channel")
            false
        }
    }

    return ::inner
}

/**
 * Check asserting that an [Event] did **not** fire within a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channel The channel to compare to.
 */
public fun notInChannel(channel: ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInChannel")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        return if (eventChannel.id != channel.id) {
            logger.passed()
            true
        } else {
            logger.failed("Channel $eventChannel is the same as channel $channel")
            false
        }
    }

    return ::inner
}

/**
 * Check asserting that an [Event] fired within a given channel category.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param category The category to check against.
 */
public fun inCategory(category: CategoryBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inCategory")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        val channels = category.channels.toList().map { it.id }

        return if (channels.contains(eventChannel.id)) {
            logger.passed()
            true
        } else {
            logger.failed("Channel $eventChannel is not in category $category")
            false
        }
    }

    return ::inner
}

/**
 * Check asserting that an [Event] did **not** fire within a given channel category.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param category The category to check against.
 */
public fun notInCategory(category: CategoryBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInCategory")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        val channels = category.channels.toList().map { it.id }

        return if (channels.contains(eventChannel.id)) {
            logger.failed("Channel $eventChannel is in category $category")
            false
        } else {
            logger.passed()
            true
        }
    }

    return ::inner
}

/**
 * Check asserting that the channel an [Event] fired in is higher than a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channel The channel to compare to.
 */
public fun channelHigher(channel: ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelHigher")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        return if (eventChannel > channel) {
            logger.passed()
            true
        } else {
            logger.failed("Channel $eventChannel is lower than or equal to $channel")
            false
        }
    }

    return ::inner
}

/**
 * Check asserting that the channel an [Event] fired in is lower than a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channel The channel to compare to.
 */
public fun channelLower(channel: ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelLower")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        return if (eventChannel < channel) {
            logger.passed()
            true
        } else {
            logger.failed("Channel $eventChannel is higher than or equal to $channel")
            false
        }
    }

    return ::inner
}

/**
 * Check asserting that the channel an [Event] fired in is higher than or equal to a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channel The channel to compare to.
 */
public fun channelHigherOrEqual(channel: ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelHigherOrEqual")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        return if (eventChannel >= channel) {
            logger.passed()
            true
        } else {
            logger.failed("Channel $eventChannel is lower than $channel")
            false
        }
    }

    return ::inner
}

/**
 * Check asserting that the channel an [Event] fired in is lower than or equal to a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channel The channel to compare to.
 */
public fun channelLowerOrEqual(channel: ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelLowerOrEqual")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        return if (eventChannel <= channel) {
            logger.passed()
            true
        } else {
            logger.failed("Channel $eventChannel is higher than $channel")
            false
        }
    }

    return ::inner
}
