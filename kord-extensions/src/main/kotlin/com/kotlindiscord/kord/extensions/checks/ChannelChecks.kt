@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.CategoryBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.entity.channel.Category
import dev.kord.core.event.Event
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

// region: Entity DSL versions

/**
 * Check asserting that an [Event] fired within a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the channel to compare to.
 */
public fun inChannel(builder: suspend () -> ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inChannel")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        val channel = builder()

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
 * @param builder Lambda returning the channel to compare to.
 */
public fun notInChannel(builder: suspend () -> ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInChannel")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        val channel = builder()

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
 * @param builder Lambda returning the category to compare to.
 */
public fun inCategory(builder: suspend () -> CategoryBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inCategory")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        val category = builder()
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
 * @param builder Lambda returning the category to compare to.
 */
public fun notInCategory(builder: suspend () -> CategoryBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInCategory")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        val category = builder()
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
 * @param builder Lambda returning the channel to compare to.
 */
public fun channelHigher(builder: suspend () -> ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelHigher")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        val channel = builder()

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
 * @param builder Lambda returning the channel to compare to.
 */
public fun channelLower(builder: suspend () -> ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelLower")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        val channel = builder()

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
 * @param builder Lambda returning the channel to compare to.
 */
public fun channelHigherOrEqual(builder: suspend () -> ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelHigherOrEqual")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        val channel = builder()

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
 * @param builder Lambda returning the channel to compare to.
 */
public fun channelLowerOrEqual(builder: suspend () -> ChannelBehavior): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelLowerOrEqual")

    suspend fun inner(event: Event): Boolean {
        val eventChannel = channelFor(event)

        if (eventChannel == null) {
            logger.nullChannel(event)
            return false
        }

        val channel = builder()

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

// endregion

// region: Snowflake versions

/**
 * Check asserting that an [Event] fired within a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Channel snowflake to compare to.
 */
public fun inChannel(id: Snowflake): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inChannel")

    suspend fun inner(event: Event): Boolean {
        val channel = event.kord.getChannel(id)

        if (channel == null) {
            logger.noChannelId(id)
            return false
        }

        return inChannel { channel }(event)
    }

    return ::inner
}

/**
 * Check asserting that an [Event] did **not** fire within a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Channel snowflake to compare to.
 */
public fun notInChannel(id: Snowflake): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInChannel")

    suspend fun inner(event: Event): Boolean {
        val channel = event.kord.getChannel(id)

        if (channel == null) {
            logger.noChannelId(id)
            return false
        }

        return notInChannel { channel }(event)
    }

    return ::inner
}

/**
 * Check asserting that an [Event] fired within a given channel category.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Category snowflake to compare to.
 */
public fun inCategory(id: Snowflake): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inCategory")

    suspend fun inner(event: Event): Boolean {
        val category = event.kord.getChannelOf<Category>(id)

        if (category == null) {
            logger.noCategoryId(id)
            return false
        }

        return inCategory { category }(event)
    }

    return ::inner
}

/**
 * Check asserting that an [Event] did **not** fire within a given channel category.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Category snowflake to compare to.
 */
public fun notInCategory(id: Snowflake): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInCategory")

    suspend fun inner(event: Event): Boolean {
        val category = event.kord.getChannelOf<Category>(id)

        if (category == null) {
            logger.noCategoryId(id)
            return false
        }

        return notInCategory { category }(event)
    }

    return ::inner
}

/**
 * Check asserting that the channel an [Event] fired in is higher than a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Channel snowflake to compare to.
 */
public fun channelHigher(id: Snowflake): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelHigher")

    suspend fun inner(event: Event): Boolean {
        val channel = event.kord.getChannel(id)

        if (channel == null) {
            logger.noChannelId(id)
            return false
        }

        return channelHigher { channel }(event)
    }

    return ::inner
}

/**
 * Check asserting that the channel an [Event] fired in is lower than a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Channel snowflake to compare to.
 */
public fun channelLower(id: Snowflake): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelLower")

    suspend fun inner(event: Event): Boolean {
        val channel = event.kord.getChannel(id)

        if (channel == null) {
            logger.noChannelId(id)
            return false
        }

        return channelLower { channel }(event)
    }

    return ::inner
}

/**
 * Check asserting that the channel an [Event] fired in is higher than or equal to a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Channel snowflake to compare to.
 */
public fun channelHigherOrEqual(id: Snowflake): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelHigherOrEqual")

    suspend fun inner(event: Event): Boolean {
        val channel = event.kord.getChannel(id)

        if (channel == null) {
            logger.noChannelId(id)
            return false
        }

        return channelHigherOrEqual { channel }(event)
    }

    return ::inner
}

/**
 * Check asserting that the channel an [Event] fired in is lower than or equal to a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Channel snowflake to compare to.
 */
public fun channelLowerOrEqual(id: Snowflake): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelLowerOrEqual")

    suspend fun inner(event: Event): Boolean {
        val channel = event.kord.getChannel(id)

        if (channel == null) {
            logger.noChannelId(id)
            return false
        }

        return channelLowerOrEqual { channel }(event)
    }

    return ::inner
}

// endregion
