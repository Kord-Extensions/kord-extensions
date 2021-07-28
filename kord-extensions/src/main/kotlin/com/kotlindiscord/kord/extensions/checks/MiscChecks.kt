package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.Check
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.event.Event
import mu.KotlinLogging
import java.util.*

/**
 * Check asserting the user for an [Event] is a bot. Will fail if the event doesn't concern a user.
 *
 * @param event Event object to check.
 */
public val isBot: Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.isBot")
    val user = userFor(event)?.asUserOrNull()

    if (user == null) {
        logger.failed("Event did not concern a user.")

        fail()
    } else if (user.isBot) {
        logger.passed()

        pass()
    } else {
        logger.failed("User is not a bot.")

        fail(
            translate("checks.isBot.failed")
        )
    }
}

/**
 * Check asserting the user for an [Event] is **not** a bot. Will fail if the event doesn't concern a user.
 *
 * @param event Event object to check.
 */
public val isNotbot: Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.isNotBot")
    val user = userFor(event)?.asUserOrNull()

    if (user == null) {
        logger.failed("Event did not concern a user.")

        fail()
    } else if (!user.isBot) {
        logger.passed()

        pass()
    } else {
        logger.failed("User is a bot.")

        fail(
            translate("checks.isNotBot.failed")
        )
    }
}

/**
 * Check asserting that the event was triggered within a thread.
 */
public val isInThread: Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.isInThread")
    val channel = channelFor(event)?.asChannelOrNull()

    if (channel == null) {
        logger.failed("Event did not concern a channel.")

        fail()
    } else if (channel is ThreadChannelBehavior) {
        logger.passed()

        pass()
    } else {
        logger.failed("Channel is not a thread.")

        fail(
            translate("checks.isInThread.failed")
        )
    }
}

/**
 * Check asserting that the event was **not** triggered within a thread, including events that don't concern any
 * specific channel.
 */
public val isNotInThread: Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.isNotInThread")
    val channel = channelFor(event)?.asChannelOrNull()

    if (channel == null) {
        logger.passed("Event did not concern a channel.")

        pass()
    } else if (channel !is ThreadChannelBehavior) {
        logger.passed()

        pass()
    } else {
        logger.failed("Channel is a thread.")

        fail(
            translate("checks.isNotInThread.failed")
        )
    }
}
