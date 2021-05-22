package com.kotlindiscord.kord.extensions.checks

import dev.kord.core.event.Event
import mu.KotlinLogging

/**
 * Check asserting the user for an [Event] is a bot. Will fail if the event doesn't concern a user.
 *
 * @param event Event object to check.
 */
public suspend fun isBot(event: Event): Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.isBot")

    val user = userFor(event)?.asUserOrNull()

    return if (user == null) {
        logger.failed("Event did not concern a user.")

        true
    } else if (user.isBot) {
        logger.passed()

        false
    } else {
        logger.failed("User is not a bot.")

        false
    }
}

/**
 * Check asserting the user for an [Event] is **not** a bot. Will fail if the event doesn't concern a user.
 *
 * @param event Event object to check.
 */
public suspend fun isNotBot(event: Event): Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.isNotBot")

    val user = userFor(event)?.asUserOrNull()

    return if (user == null) {
        logger.failed("Event did not concern a user.")

        true
    } else if (!user.isBot) {
        logger.passed()

        false
    } else {
        logger.failed("User is a bot.")

        false
    }
}
