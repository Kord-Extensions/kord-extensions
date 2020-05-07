@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.gitlab.kordlib.common.entity.ChannelType
import com.gitlab.kordlib.core.event.message.MessageCreateEvent

/**
 * Check asserting that the channel a [MessageCreateEvent] fired in is of a given type.
 *
 * @param channelType The channel type to compare to.
 */
fun channelType(channelType: ChannelType): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            return message.channel.asChannel().type == channelType
        }
    }

    return ::inner
}

/**
 * Check asserting that the channel a [MessageCreateEvent] fired in is **not** of a given type.
 *
 * @param channelType The channel type to compare to.
 */
fun notChannelType(channelType: ChannelType): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            return message.channel.asChannel().type != channelType
        }
    }

    return ::inner
}
