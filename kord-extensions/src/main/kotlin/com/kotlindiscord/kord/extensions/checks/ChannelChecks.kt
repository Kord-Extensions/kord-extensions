@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.gitlab.kordlib.core.behavior.channel.CategoryBehavior
import com.gitlab.kordlib.core.behavior.channel.ChannelBehavior
import com.gitlab.kordlib.core.event.Event
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.kotlindiscord.kord.extensions.InvalidEventHandlerException
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import kotlin.math.log

/**
 * Check asserting that an [Event] fired within a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channel The channel to compare to.
 */
fun inChannel(channel: ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)
        
        if (eventChannel == null) {
            logger.debug { "Channel for event $event is null. This type of event may not be supported." }
            return false
        }

        return eventChannel.id == channel.id
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
fun notInChannel(channel: ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.debug { "Channel for event $event is null. This type of event may not be supported." }
            return false
        }

        return eventChannel.id != channel.id
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
fun inCategory(category: CategoryBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.debug { "Channel for event $event is null. This type of event may not be supported." }
            return false
        }

        val channels = category.channels.toList().map { it.id }

        return channels.contains(eventChannel.id)
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
fun notInCategory(category: CategoryBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.debug { "Channel for event $event is null. This type of event may not be supported." }
            return false
        }

        val channels = category.channels.toList().map { it.id }

        return channels.contains(eventChannel.id).not()
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
fun channelHigher(channel: ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.debug { "Channel for event $event is null. This type of event may not be supported." }
            return false
        }

        return eventChannel > channel
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
fun channelLower(channel: ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.debug { "Channel for event $event is null. This type of event may not be supported." }
            return false
        }

        return eventChannel < channel
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
fun channelHigherOrEqual(channel: ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.debug { "Channel for event $event is null. This type of event may not be supported." }
            return false
        }

        return eventChannel >= channel
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
fun channelLowerOrEqual(channel: ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.debug { "Channel for event $event is null. This type of event may not be supported." }
            return false
        }

        return eventChannel <= channel
    }

    return ::inner
}
