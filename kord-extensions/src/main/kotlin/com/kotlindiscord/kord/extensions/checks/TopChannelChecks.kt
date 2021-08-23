@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.Check
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.event.Event
import mu.KotlinLogging

// region: Entity DSL versions

/**
 * Check asserting that an [Event] fired within a given channel. If the event fired within a thread,
 * it checks the thread's parent channel instead.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the channel to compare to.
 */
public fun <T : Event> inTopChannel(builder: suspend (T) -> ChannelBehavior): Check<T> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inChannel")
    val eventChannel = topChannelFor(event)

    if (eventChannel == null) {
        logger.nullChannel(event)

        fail()
    } else {
        val channel = builder(event)

        if (eventChannel.id == channel.id) {
            logger.passed()

            pass()
        } else {
            logger.failed("Channel $eventChannel is not the same as channel $channel")

            fail(
                translate(
                    "checks.inChannel.failed",
                    replacements = arrayOf(channel.mention),
                )
            )
        }
    }
}

/**
 * Check asserting that an [Event] did **not** fire within a given channel. If the event fired within a thread,
 * it checks the thread's parent channel instead.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the channel to compare to.
 */
public fun <T : Event> notInTopChannel(builder: suspend (T) -> ChannelBehavior): Check<T> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInChannel")
    val eventChannel = topChannelFor(event)

    if (eventChannel == null) {
        logger.nullChannel(event)

        pass()
    } else {
        val channel = builder(event)

        if (eventChannel.id != channel.id) {
            logger.passed()

            pass()
        } else {
            logger.failed("Channel $eventChannel is the same as channel $channel")

            fail(
                translate(
                    "checks.notInChannel.failed",
                    replacements = arrayOf(channel.mention)
                )
            )
        }
    }
}

// endregion

// region: Snowflake versions

/**
 * Check asserting that an [Event] fired within a given channel. If the event fired within a thread,
 * it checks the thread's parent channel instead.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Channel snowflake to compare to.
 */
public fun <T : Event> inTopChannel(id: Snowflake): Check<T> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inChannel")
    var channel = event.kord.getChannel(id)

    if (channel is ThreadChannel) {
        channel = channel.parent.asChannel()
    }

    if (channel == null) {
        logger.noChannelId(id)

        fail()
    } else {
        inChannel<T> { channel }()
    }
}

/**
 * Check asserting that an [Event] did **not** fire within a given channel. If the event fired within a thread,
 * it checks the thread's parent channel instead.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Channel snowflake to compare to.
 */
public fun <T : Event> notInTopChannel(id: Snowflake): Check<T> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notInChannel")
    var channel = event.kord.getChannel(id)

    if (channel is ThreadChannel) {
        channel = channel.parent.asChannel()
    }

    if (channel == null) {
        logger.noChannelId(id)

        pass()
    } else {
        notInChannel<T> { channel }()
    }
}

// endregion
