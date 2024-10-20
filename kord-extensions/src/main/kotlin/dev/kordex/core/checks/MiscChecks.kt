/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.core.checks

import dev.kord.common.entity.TeamMemberRole
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.event.Event
import dev.kordex.core.checks.types.CheckContext
import dev.kordex.core.i18n.generated.CoreTranslations
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * For bots with single owners, check asserting the user for an [Event] is the bot's owner.
 *
 * Will fail if the event doesn't concern a user, or the bot doesn't have a single owner (e.g. it is part of a team).
 */
public suspend fun CheckContext<*>.isBotOwner() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.isBotOwner")
	val owner = event.kord.getApplicationInfo().ownerId

	if (owner == null) {
		logger.failed("Bot does not have an owner.")

		return fail()
	}

	val user = userFor(event)?.asUserOrNull()

	if (user == null) {
		logger.failed("Event did not concern a user.")

		fail()
	} else if (user.id == owner) {
		logger.passed()

		pass()
	} else {
		logger.failed("User does not own this bot.")

		fail(CoreTranslations.Checks.IsBotOwner.failed)
	}
}

/**
 * For bots with single owners, check asserting the user for an [Event] is not the bot's owner.
 *
 * Will pass if the event doesn't concern a user, or the bot doesn't have a single owner (e.g. it is part of a team).
 */
public suspend fun CheckContext<*>.isNotBotOwner() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.isNotBotOwner")
	val owner = event.kord.getApplicationInfo().ownerId

	if (owner == null) {
		logger.passed("Bot does not have an owner.")

		return pass()
	}

	val user = userFor(event)?.asUserOrNull()

	if (user == null) {
		logger.passed("Event did not concern a user.")

		pass()
	} else if (user.id == owner) {
		logger.failed("User owns this bot.")

		fail(CoreTranslations.Checks.IsNotBotOwner.failed)
	} else {
		logger.failed("User does not own this bot.")

		pass()
	}
}

/**
 * For bots owned by a team, check asserting the user for an [Event] is one of the bot's admins.
 *
 * Will fail if the event doesn't concern a user, or the bot doesn't have any admins (e.g. it has a single owner).
 */
public suspend fun CheckContext<*>.isBotAdmin() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.isBotAdmin")
	val admins = event.kord.getApplicationInfo().team
		?.members
		?.filter { it.role == TeamMemberRole.Admin }
		?.map { it.userId }

	if (admins.isNullOrEmpty()) {
		logger.failed("Bot does not have any admins.")

		return fail()
	}

	val user = userFor(event)?.asUserOrNull()

	if (user == null) {
		logger.failed("Event did not concern a user.")

		fail()
	} else if (user.id in admins) {
		logger.passed()

		pass()
	} else {
		logger.failed("User does not administrate this bot.")

		fail(CoreTranslations.Checks.IsBotAdmin.failed)
	}
}

/**
 * For bots owned by a team, check asserting the user for an [Event] is not one of the bot's admins.
 *
 * Will pass if the event doesn't concern a user, or the bot doesn't have any admins (e.g. it has a single owner).
 */
public suspend fun CheckContext<*>.isNotBotAdmin() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.isNotBotAdmin")
	val admins = event.kord.getApplicationInfo().team
		?.members
		?.filter { it.role == TeamMemberRole.Admin }
		?.map { it.userId }

	if (admins.isNullOrEmpty()) {
		logger.passed("Bot does not have any admins.")

		return pass()
	}

	val user = userFor(event)?.asUserOrNull()

	if (user == null) {
		logger.passed("Event did not concern a user.")

		pass()
	} else if (user.id in admins) {
		logger.failed("User administrates this bot.")

		fail(CoreTranslations.Checks.IsNotBotAdmin.failed)
	} else {
		logger.passed("User does not administrate this bot.")

		pass()
	}
}

/**
 * Check asserting the user for an [Event] is a bot. Will fail if the event doesn't concern a user.
 */
public suspend fun CheckContext<*>.isBot() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.isBot")
	val user = userFor(event)?.asUserOrNull()

	if (user == null) {
		logger.failed("Event did not concern a user.")

		fail()
	} else if (user.isBot) {
		logger.passed()

		pass()
	} else {
		logger.failed("User is not a bot.")

		fail(CoreTranslations.Checks.IsBot.failed)
	}
}

/**
 * Check asserting the user for an [Event] is **not** a bot. Will pass if the event doesn't concern a user.
 */
public suspend fun CheckContext<*>.isNotBot() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.isNotBot")
	val user = userFor(event)?.asUserOrNull()

	if (user == null) {
		logger.passed("Event did not concern a user.")

		pass()
	} else if (!user.isBot) {
		logger.passed()

		pass()
	} else {
		logger.failed("User is a bot.")

		fail(CoreTranslations.Checks.IsNotBot.failed)
	}
}

/**
 * Check asserting that the event was triggered within a thread.
 */
public suspend fun CheckContext<*>.isInThread() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.isInThread")

	when (channelFor(event)?.asChannelOrNull()) {
		null -> {
			logger.failed("Event did not concern a channel.")

			fail()
		}

		is ThreadChannelBehavior -> {
			logger.passed()

			pass()
		}

		else -> {
			logger.failed("Channel is not a thread.")

			fail(CoreTranslations.Checks.IsInThread.failed)
		}
	}
}

/**
 * Check asserting that the event was **not** triggered within a thread, including events that don't concern any
 * specific channel.
 */
public suspend fun CheckContext<*>.isNotInThread() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.isNotInThread")

	when (channelFor(event)?.asChannelOrNull()) {
		null -> {
			logger.passed("Event did not concern a channel.")

			pass()
		}

		!is ThreadChannelBehavior -> {
			logger.passed()

			pass()
		}

		else -> {
			logger.failed("Channel is a thread.")

			fail(CoreTranslations.Checks.IsNotInThread.failed)
		}
	}
}
