/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.kord.common.entity.TeamMemberRole
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.event.Event
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

	val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.isBotOwner")
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

		fail(
			translate("checks.isBotOwner.failed")
		)
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

	val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.isBotAdmin")
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

		fail(
			translate("checks.isBotAdmin.failed")
		)
	}
}

/**
 * Check asserting the user for an [Event] is a bot. Will fail if the event doesn't concern a user.
 */
public suspend fun CheckContext<*>.isBot() {
	if (!passed) {
		return
	}

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
 */
public suspend fun CheckContext<*>.isNotBot() {
	if (!passed) {
		return
	}

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
public suspend fun CheckContext<*>.isInThread() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.isInThread")

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

			fail(
				translate("checks.isInThread.failed")
			)
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

	val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.isNotInThread")

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

			fail(
				translate("checks.isNotInThread.failed")
			)
		}
	}
}
