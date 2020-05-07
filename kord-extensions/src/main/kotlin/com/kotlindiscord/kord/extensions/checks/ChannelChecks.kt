@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.gitlab.kordlib.core.entity.channel.Category
import com.gitlab.kordlib.core.entity.channel.Channel
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.toList

/**
 * Check asserting that a [MessageCreateEvent] fired within a given channel.
 *
 * @param channel The channel to compare to.
 */
fun inChannel(channel: Channel): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            return message.channel.id == channel.id
        }
    }

    return ::inner
}

/**
 * Check asserting that a [MessageCreateEvent] did **not** fire within a given channel.
 *
 * @param channel The channel to compare to.
 */
fun notInChannel(channel: Channel): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            return message.channel.id != channel.id
        }
    }

    return ::inner
}

/**
 * Check asserting that a [MessageCreateEvent] fired within a given channel category.
 *
 * @param category The category to check against.
 */
fun inCategory(category: Category): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        val channels = category.channels.toList().map { it.id }

        with(event) {
            return channels.contains(message.channel.id)
        }
    }

    return ::inner
}

/**
 * Check asserting that a [MessageCreateEvent] did **not** fire within a given channel category.
 *
 * @param category The category to check against.
 */
fun notInCategory(category: Category): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        val channels = category.channels.toList().map { it.id }

        with(event) {
            return channels.contains(message.channel.id).not()
        }
    }

    return ::inner
}

/**
 * Check asserting that the channel a [MessageCreateEvent] fired in is higher than a given channel.
 *
 * @param channel The channel to compare to.
 */
fun channelHigher(channel: Channel): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            return message.channel > channel
        }
    }

    return ::inner
}

/**
 * Check asserting that the channel a [MessageCreateEvent] fired in is lower than a given channel.
 *
 * @param channel The channel to compare to.
 */
fun channelLower(channel: Channel): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            return message.channel < channel
        }
    }

    return ::inner
}

/**
 * Check asserting that the channel a [MessageCreateEvent] fired in is higher than or equal to a given channel.
 *
 * @param channel The channel to compare to.
 */
fun channelHigherOrEqual(channel: Channel): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            return message.channel >= channel
        }
    }

    return ::inner
}

/**
 * Check asserting that the channel a [MessageCreateEvent] fired in is lower than or equal to a given channel.
 *
 * @param channel The channel to compare to.
 */
fun channelLowerOrEqual(channel: Channel): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            return message.channel <= channel
        }
    }

    return ::inner
}
