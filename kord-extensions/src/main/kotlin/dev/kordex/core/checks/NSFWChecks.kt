/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")
@file:OptIn(NotTranslated::class)

package dev.kordex.core.checks

import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.NsfwLevel
import dev.kord.core.event.Event
import dev.kordex.core.annotations.NotTranslated
import dev.kordex.core.checks.types.CheckContext
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.utils.compareTo
import dev.kordex.core.utils.toTranslationKey
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Check asserting an [Event] was fired within a guild with the given NSFW level.
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.hasGuildNsfwLevel(level: NsfwLevel) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.hasGuildNsfwLevel")
	val guild = guildFor(event)?.asGuildOrNull()

	if (guild == null) {
		logger.failed("Event did not happen within a guild.")

		fail(CoreTranslations.Checks.AnyGuild.failed)
	} else {
		if (guild.nsfw == level) {
			logger.passed()

			pass()
		} else {
			logger.failed("Guild did not have the correct NSFW level: $level")

			fail(
				CoreTranslations.Checks.GuildNsfwLevelEqual.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(level.toTranslationKey())
			)
		}
	}
}

/**
 * Check asserting an [Event] was fired within a guild without the given NSFW level.
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.notHasGuildNsfwLevel(level: NsfwLevel) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notHasGuildNsfwLevel")
	val guild = guildFor(event)?.asGuildOrNull()

	if (guild == null) {
		logger.passed("Event did not happen within a guild.")

		pass()
	} else {
		if (guild.nsfw == level) {
			logger.failed("Guild matched the given NSFW level: $level")

			fail(
				CoreTranslations.Checks.GuildNsfwLevelNotEqual.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(level.toTranslationKey())
			)
		} else {
			logger.passed()

			pass()
		}
	}
}

/**
 * Check asserting an [Event] was fired within a guild with a NSFW level higher than the provided one..
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.guildNsfwLevelHigher(level: NsfwLevel) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.guildNsfwLevelHigher")
	val guild = guildFor(event)?.asGuildOrNull()

	if (guild == null) {
		logger.failed("Event did not happen within a guild.")

		fail(CoreTranslations.Checks.AnyGuild.failed)
	} else {
		if (guild.nsfw > level) {
			logger.passed()

			pass()
		} else {
			logger.failed("Guild did not have a NSFW level higher than: $level")

			fail(
				CoreTranslations.Checks.GuildNsfwLevelHigher.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(level.toTranslationKey())
			)
		}
	}
}

/**
 * Check asserting an [Event] was fired within a guild with a NSFW level higher than or equal to the provided one..
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.guildNsfwLevelHigherOrEqual(level: NsfwLevel) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.guildNsfwLevelHigherOrEqual")
	val guild = guildFor(event)?.asGuildOrNull()

	if (guild == null) {
		logger.failed("Event did not happen within a guild.")

		fail(CoreTranslations.Checks.AnyGuild.failed)
	} else {
		if (guild.nsfw >= level) {
			logger.passed()

			pass()
		} else {
			logger.failed("Guild did not have a NSFW level higher than or equal to: $level")

			fail(
				CoreTranslations.Checks.GuildNsfwLevelHigherOrEqual.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(level.toTranslationKey())
			)
		}
	}
}

/**
 * Check asserting an [Event] was fired within a guild with a NSFW level lower than the provided one..
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.guildNsfwLevelLower(level: NsfwLevel) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.guildNsfwLevelLower")
	val guild = guildFor(event)?.asGuildOrNull()

	if (guild == null) {
		logger.failed("Event did not happen within a guild.")

		fail(CoreTranslations.Checks.AnyGuild.failed)
	} else {
		if (guild.nsfw < level) {
			logger.passed()

			pass()
		} else {
			logger.failed("Guild did not have a NSFW level lower than: $level")

			fail(
				CoreTranslations.Checks.GuildNsfwLevelLower.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(level.toTranslationKey())
			)
		}
	}
}

/**
 * Check asserting an [Event] was fired within a guild with a NSFW level lower than or equal to the provided one..
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.guildNsfwLevelLowerOrEqual(level: NsfwLevel) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.guildNsfwLevelLowerOrEqual")
	val guild = guildFor(event)?.asGuildOrNull()

	if (guild == null) {
		logger.failed("Event did not happen within a guild.")

		fail(CoreTranslations.Checks.AnyGuild.failed)
	} else {
		if (guild.nsfw <= level) {
			logger.passed()

			pass()
		} else {
			logger.failed("Guild did not have a NSFW level lower than or equal to: $level")

			fail(
				CoreTranslations.Checks.GuildNsfwLevelLowerOrEqual.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(level.toTranslationKey())
			)
		}
	}
}

/**
 * Check asserting that the channel an [Event] fired in is marked as NSFW.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 */
public suspend fun CheckContext<*>.channelIsNsfw() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.channelIsNsfw")
	val eventChannel = channelFor(event)?.asChannel()

	if (eventChannel == null) {
		logger.nullChannel(event)

		fail()
	} else {
		if (eventChannel.data.nsfw.discordBoolean) {
			logger.passed()

			pass()
		} else {
			logger.failed("Channel is not marked as NSFW")

			fail(CoreTranslations.Checks.ChannelIsNsfw.failed)
		}
	}
}

/**
 * Check asserting that the channel an [Event] fired in is not marked as NSFW.
 *
 * DM channels can't be marked as NSFW, so this will always pass for a DM channel.
 */
public suspend fun CheckContext<*>.notChannelIsNsfw() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notChannelIsNsfw")
	val eventChannel = channelFor(event)?.asChannel()

	if (eventChannel == null) {
		logger.nullChannel(event)

		pass()
	} else if (eventChannel.type == ChannelType.DM) {
		logger.passed()

		pass()
	} else {
		if (!eventChannel.data.nsfw.discordBoolean) {
			logger.passed()

			pass()
		} else {
			logger.failed("Channel is marked as NSFW")

			fail(CoreTranslations.Checks.NotChannelIsNsfw.failed)
		}
	}
}

/**
 * Check asserting that the channel an [Event] fired in is marked as NSFW, or is in an NSFW guild.
 */
public suspend fun CheckContext<*>.channelOrGuildIsNsfw() {
	channelIsNsfw()
	or { guildNsfwLevelHigherOrEqual(NsfwLevel.AgeRestricted) }
}

/**
 * Check asserting that the channel an [Event] fired in is not marked as NSFW, and is not in an NSFW guild.
 */
public suspend fun CheckContext<*>.notChannelOrGuildIsNsfw() {
	notChannelIsNsfw()
	or { guildNsfwLevelLower(NsfwLevel.AgeRestricted) }
}
