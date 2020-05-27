@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.gitlab.kordlib.common.entity.ChannelType
import com.gitlab.kordlib.core.event.Event
import mu.KotlinLogging

/**
 * Check asserting that the channel an [Event] fired in is of a given set of types.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channelTypes The channel types to compare to.
 */
fun channelType(vararg channelTypes: ChannelType): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.debug { "Channel for event $event is null. This type of event may not be supported." }
            return false
        }

        return channelTypes.contains(eventChannel.asChannel().type)
    }

    return ::inner
}

/**
 * Check asserting that the channel an [Event] fired in is **not** of a given set of types.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channelTypes The channel types to compare to.
 */
fun notChannelType(vararg channelTypes: ChannelType): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.debug { "Channel for event $event is null. This type of event may not be supported." }
            return false
        }

        return channelTypes.contains(eventChannel.asChannel().type).not()
    }

    return ::inner
}
