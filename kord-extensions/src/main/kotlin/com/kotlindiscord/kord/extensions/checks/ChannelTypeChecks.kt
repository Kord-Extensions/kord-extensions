@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.utils.getKoin
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.common.entity.ChannelType
import dev.kord.core.event.Event
import mu.KotlinLogging
import java.util.*

private val defaultLocale: Locale
    get() =
        getKoin().get<ExtensibleBotBuilder>().i18nBuilder.defaultLocale

/**
 * Check asserting that the channel an [Event] fired in is of a given set of types.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channelTypes The channel types to compare to.
 */
public fun channelType(vararg channelTypes: ChannelType): Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelType")
    val eventChannel = channelFor(event)

    if (eventChannel == null) {
        logger.nullChannel(event)

        fail()
    } else {
        val type = eventChannel.asChannel().type

        if (channelTypes.contains(type)) {
            logger.passed()

            pass()
        } else {
            logger.failed("Types $type is not within $channelTypes")

            fail(
                translate(
                    "checks.channelType.failed",
                    replacements = arrayOf(type.translate(locale)),
                )
            )
        }
    }
}

/**
 * Check asserting that the channel an [Event] fired in is **not** of a given set of types.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channelTypes The channel types to compare to.
 */
public fun notChannelType(vararg channelTypes: ChannelType): Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notChannelType")
    val eventChannel = channelFor(event)

    if (eventChannel == null) {
        logger.nullChannel(event)

        fail()
    } else {
        val type = eventChannel.asChannel().type

        if (channelTypes.contains(type)) {
            logger.failed("Types $type is within $channelTypes")

            fail(
                translate(
                    "checks.notChannelType.failed",
                    replacements = arrayOf(type.translate(locale)),
                )
            )
        } else {
            logger.passed()

            pass()
        }
    }
}
