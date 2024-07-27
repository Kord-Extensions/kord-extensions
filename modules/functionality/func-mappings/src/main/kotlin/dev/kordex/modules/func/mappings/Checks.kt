/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.CategorizableChannel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.checks.channelFor
import dev.kordex.core.checks.guildFor
import dev.kordex.core.checks.types.CheckContext
import io.github.oshai.kotlinlogging.KotlinLogging
/**
 * Check ensuring that a command was sent within an allowed category, or not within a banned one.
 *
 * Passes when:
 * * Command doesn't happen in a channel
 * * Command happens in a channel that can't be in a category (eg, DMs)
 * * Command happens in an allowed category
 * * If there are no allowed categories, command doesn't happen in a banned category
 *
 * Fails when:
 * * Command happens outside a category, when allowed categories are configured
 * * If there are allowed categories, command doesn't happen in an allowed category
 * * Command happens in a banned category
 *
 * @param allowed List of allowed category IDs
 * @param banned List of banned category IDs
 */
suspend fun CheckContext<ChatInputCommandInteractionCreateEvent>.allowedCategory(
	allowed: List<Snowflake>,
	banned: List<Snowflake>,
) {
	val logger = KotlinLogging.logger { }
	val channel = channelFor(event)

	if (channel == null) {
		logger.trace { "Passing: Event is not channel-related" }

		pass()
	} else if (channel !is CategorizableChannel) {
		logger.trace { "Passing: Channel is not categorizable (eg, it's a DM)" }

		pass()
	} else {
		val parent = channel.category

		if (allowed.isNotEmpty()) {
			if (parent == null) {
				logger.debug { "Failing: We have allowed categories, but the command was sent outside of a category" }

				fail()
			} else if (allowed.contains(parent.id)) {
				logger.trace { "Passing: Event happened in an allowed category" }

				pass()
			} else {
				logger.debug { "Failing: Event happened outside of the allowed categories" }

				fail()
			}
		} else {
			if (parent == null) {
				logger.trace {
					"Passing: We have no allowed categories, and the command was sent outside of a category"
				}

				pass()
			} else if (banned.isNotEmpty()) {
				if (!banned.contains(parent.id)) {
					logger.trace { "Passing: Event did not happen in a banned category" }

					pass()
				} else {
					logger.debug { "Failing: Event happened in a banned category" }

					fail()
				}
			} else {
				logger.trace { "Passing: No allowed or banned categories configured" }

				pass()
			}
		}
	}
}

/**
 * Check ensuring that a command was sent within an allowed channel, or not within a banned one.
 *
 * Passes when:
 * * Command doesn't happen in a channel
 * * Command happens in a channel that can't be in a guild (eg, DMs)
 * * Command happens in an allowed channel
 * * If there are no allowed channels, command doesn't happen in a banned channel
 *
 * Fails when:
 * * If there are allowed channels, command doesn't happen in an allowed channel
 * * Command happens in a banned channel
 *
 * @param allowed List of allowed channel IDs
 * @param banned List of banned channel IDs
 */
suspend fun CheckContext<ChatInputCommandInteractionCreateEvent>.allowedChannel(
	allowed: List<Snowflake>,
	banned: List<Snowflake>,
) {
	val logger = KotlinLogging.logger { }
	val channel = channelFor(event)

	if (channel == null) {
		logger.trace { "Passing: Event is not channel-related" }

		pass()
	} else if (channel !is GuildChannel) {
		logger.trace { "Passing: Command was sent privately" }

		pass()  // It's a DM
	} else if (allowed.isNotEmpty()) {
		if (allowed.contains(channel.id)) {
			logger.trace { "Passing: Event happened in an allowed channel" }

			pass()
		} else {
			logger.debug { "Failing: Event did not happen in an allowed channel" }

			fail()
		}
	} else if (banned.isNotEmpty()) {
		if (!banned.contains(channel.id)) {
			logger.trace { "Passing: Event did not happen in a banned channel" }

			pass()
		} else {
			logger.debug { "Failing: Event happened in a banned channel" }

			fail()
		}
	} else {
		logger.trace { "Passing: No allowed or banned channels configured" }

		pass()
	}
}

/**
 * Check ensuring that a command was sent within an allowed guild, or not within a banned one.
 *
 * Passes when:
 * * Command doesn't happen in a guild
 * * Command happens in an allowed guild
 * * If there are no allowed guilds, command doesn't happen in a banned guild
 *
 * Fails when:
 * * If there are allowed guilds, command doesn't happen in an allowed guild
 * * Command happens in a banned guild
 *
 * @param allowed List of allowed guild IDs
 * @param banned List of banned guild IDs
 */
suspend fun CheckContext<ChatInputCommandInteractionCreateEvent>.allowedGuild(
	allowed: List<Snowflake>,
	banned: List<Snowflake>,
) {
	val logger = KotlinLogging.logger { }
	val guild = guildFor(event)

	if (guild == null) {
		logger.trace { "Passing: Event is not guild-related" }

		pass()
	} else if (allowed.isNotEmpty()) {
		if (allowed.contains(guild.id)) {
			logger.trace { "Passing: Event happened in an allowed guild" }

			pass()
		} else {
			logger.debug { "Failing: Event did not happen in an allowed guild" }

			fail()
		}
	} else if (banned.isNotEmpty()) {
		if (!banned.contains(guild.id)) {
			logger.trace { "Passing: Event did not happen in a banned guild" }

			pass()
		} else {
			logger.debug { "Failing: Event happened in a banned guild" }

			fail()
		}
	} else {
		logger.trace { "Passing: No allowed or banned guilds configured" }

		pass()
	}
}
