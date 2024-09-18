/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(NotTranslated::class)

package dev.kordex.core.checks

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.event.Event
import dev.kordex.core.annotations.NotTranslated
import dev.kordex.core.checks.types.CheckContext
import dev.kordex.core.i18n.generated.CoreTranslations
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Check asserting an [Event] was fired within a guild.
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.anyGuild() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.anyGuild")

	if (guildFor(event) != null) {
		logger.passed()

		pass()
	} else {
		logger.failed("Event did not happen within a guild.")

		fail(CoreTranslations.Checks.AnyGuild.failed)
	}
}

/**
 * Check asserting an [Event] was **not** fired within a guild.
 *
 * **Note:** This check can't tell the difference between an event that wasn't fired within a guild, and an event
 * that fired within a guild the bot doesn't have access to, or that it can't get the GuildBehavior for (for
 * example, due to a niche Kord configuration).
 */
public suspend fun CheckContext<*>.noGuild() {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.noGuild")

	if (guildFor(event) == null) {
		logger.passed()

		pass()
	} else {
		logger.failed("Event happened within a guild.")

		fail(CoreTranslations.Checks.NoGuild.failed)
	}
}

// region: Entity DSL versions

/**
 * Check asserting that the guild an [Event] fired for is in a specific guild.
 *
 * Only events that can reasonably be associated with a guild are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the guild to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.inGuild(builder: suspend (T) -> GuildBehavior) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.inGuild")
	val eventGuild = guildFor(event)?.asGuildOrNull()

	if (eventGuild == null) {
		logger.nullGuild(event)

		fail()
	} else {
		val guild = builder(event)

		if (eventGuild.id == guild.id) {
			logger.passed()

			pass()
		} else {
			logger.failed("Guild $eventGuild does not match $guild")

			fail(
				CoreTranslations.Checks.InGuild.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(eventGuild.name)
			)
		}
	}
}

/**
 * Check asserting that the guild an [Event] fired for **is not** in a specific guild.
 *
 * Only events that can reasonably be associated with a guild are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the guild to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.notInGuild(builder: suspend (T) -> GuildBehavior) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notInGuild")
	val eventGuild = guildFor(event)?.asGuild()

	if (eventGuild == null) {
		logger.nullGuild(event)

		pass()
	} else {
		val guild = builder(event)

		if (eventGuild.id != guild.id) {
			logger.passed()

			pass()
		} else {
			logger.failed("Guild $eventGuild matches $guild")

			fail(
				CoreTranslations.Checks.NotInGuild.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(eventGuild.name)
			)
		}
	}
}

// endregion

// region: Snowflake versions

/**
 * Check asserting that the guild an [Event] fired for is in a specific guild.
 *
 * Only events that can reasonably be associated with a guild are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Guild snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.inGuild(id: Snowflake) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.inGuild")
	val guild = event.kord.getGuildOrNull(id)

	if (guild == null) {
		logger.noGuildId(id)

		fail()
	} else {
		inGuild { guild }
	}
}

/**
 * Check asserting that the guild an [Event] fired for **is not** in a specific guild.
 *
 * Only events that can reasonably be associated with a guild are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Guild snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.notInGuild(id: Snowflake) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notInGuild")
	val guild = event.kord.getGuildOrNull(id)

	if (guild == null) {
		logger.noGuildId(id)

		pass()
	} else {
		notInGuild { guild }
	}
}

// endregion
