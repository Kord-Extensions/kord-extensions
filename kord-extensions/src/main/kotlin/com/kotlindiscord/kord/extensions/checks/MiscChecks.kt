package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.Check
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
