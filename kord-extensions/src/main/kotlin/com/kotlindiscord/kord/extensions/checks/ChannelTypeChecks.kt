@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import dev.kord.common.entity.ChannelType
import dev.kord.core.event.Event
import mu.KotlinLogging

/**
 * Check asserting that the channel an [Event] fired in is of a given set of types.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channelTypes The channel types to compare to.
 */
public fun channelType(vararg channelTypes: ChannelType): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelType")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        val type = eventChannel.asChannel().type

        return if (channelTypes.contains(type)) {
            logger.passed()
            true
        } else {
            logger.failed("Types $type is not within $channelTypes")
            false
        }
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
public fun notChannelType(vararg channelTypes: ChannelType): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notChannelType")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        val type = eventChannel.asChannel().type

        return if (channelTypes.contains(type)) {
            logger.failed("Types $type is within $channelTypes")
            false
        } else {
            logger.passed()
            true
        }
    }

    return ::inner
}
