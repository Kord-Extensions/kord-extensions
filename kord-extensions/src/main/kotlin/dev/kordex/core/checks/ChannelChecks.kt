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
import dev.kord.core.behavior.channel.CategoryBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.behavior.channel.GuildChannelBehavior
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.Category
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.Event
import dev.kordex.core.annotations.NotTranslated
import dev.kordex.core.checks.types.CheckContext
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.utils.isAbove
import dev.kordex.core.utils.isBelow
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.toList

// region: Entity DSL versions

/**
 * Check asserting that an [Event] fired within a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the channel to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.inChannel(builder: suspend (T) -> ChannelBehavior) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.inChannel")
	val eventChannel = channelFor(event)

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
				CoreTranslations.Checks.InChannel.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(channel.mention)
			)
		}
	}
}

/**
 * Check asserting that an [Event] did **not** fire within a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the channel to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.notInChannel(builder: suspend (T) -> ChannelBehavior) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notInChannel")
	val eventChannel = channelFor(event)

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
				CoreTranslations.Checks.NotInChannel.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(channel.mention)
			)
		}
	}
}

/**
 * Check asserting that an [Event] fired within a given channel category.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the category to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.inCategory(builder: suspend (T) -> CategoryBehavior) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.inCategory")
	val eventChannel = topChannelFor(event)

	if (eventChannel == null) {
		logger.nullChannel(event)

		fail()
	} else {
		val category = builder(event)
		val channels = category.channels.toList().map { it.id }

		if (channels.contains(eventChannel.id)) {
			logger.passed()

			pass()
		} else {
			logger.failed("Channel $eventChannel is not in category $category")

			fail(
				CoreTranslations.Checks.InCategory.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(category.asChannel().name)
			)
		}
	}
}

/**
 * Check asserting that an [Event] did **not** fire within a given channel category.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the category to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.notInCategory(builder: suspend (T) -> CategoryBehavior) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notInCategory")
	val eventChannel = topChannelFor(event)

	if (eventChannel == null) {
		logger.nullChannel(event)

		pass()
	} else {
		val category = builder(event)
		val channels = category.channels.toList().map { it.id }

		if (channels.contains(eventChannel.id)) {
			logger.failed("Channel $eventChannel is in category $category")

			fail(
				CoreTranslations.Checks.NotInCategory.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(category.asChannel().name)
			)
		} else {
			logger.passed()

			pass()
		}
	}
}

/**
 * Check asserting that the channel an [Event] fired in is above the given channel in the channel list.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the channel to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.channelAbove(builder: suspend (T) -> GuildChannelBehavior) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.channelHigher")
	val baseEventChannel = channelFor(event)

	if (baseEventChannel == null) {
		logger.nullChannel(event)

		return fail()
	}

	val eventChannel = baseEventChannel as? GuildChannelBehavior

	if (eventChannel == null) {
		logger.failed("Channel $baseEventChannel is not a thread or top channel.")

		fail()
	} else {
		val channel = builder(event)

		if (eventChannel.isAbove(channel)) {
			logger.passed()

			pass()
		} else {
			logger.failed("Channel $eventChannel is below or equal to $channel")

			fail(
				CoreTranslations.Checks.ChannelHigher.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(channel.mention)
			)
		}
	}
}

/**
 * Check asserting that the channel an [Event] fired in is below the given channel in the channel list.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the channel to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.channelBelow(builder: suspend (T) -> GuildChannelBehavior) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.channelLower")
	val baseEventChannel = channelFor(event)

	if (baseEventChannel == null) {
		logger.nullChannel(event)

		return fail()
	}

	val eventChannel = baseEventChannel as? GuildChannelBehavior

	if (eventChannel == null) {
		logger.failed("Channel $baseEventChannel is not a thread or top channel.")

		fail()
	} else {
		val channel = builder(event)

		if (eventChannel.isBelow(channel)) {
			logger.passed()

			pass()
		} else {
			logger.failed("Channel $eventChannel is above or equal to $channel")

			fail(
				CoreTranslations.Checks.ChannelLower.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(channel.mention)
			)
		}
	}
}

// endregion

// region: Snowflake versions

/**
 * Check asserting that an [Event] fired within a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Channel snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.inChannel(id: Snowflake) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.inChannel")
	val channel = event.kord.getChannel(id)

	if (channel == null) {
		logger.noChannelId(id)

		fail()
	} else {
		inChannel { channel }
	}
}

/**
 * Check asserting that an [Event] did **not** fire within a given channel.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Channel snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.notInChannel(id: Snowflake) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notInChannel")
	val channel = event.kord.getChannel(id)

	if (channel == null) {
		logger.noChannelId(id)

		pass()
	} else {
		notInChannel { channel }
	}
}

/**
 * Check asserting that an [Event] fired within a given channel category.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Category snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.inCategory(id: Snowflake) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.inCategory")
	val category = event.kord.getChannelOf<Category>(id)

	if (category == null) {
		logger.noCategoryId(id)

		fail()
	} else {
		inCategory { category }
	}
}

/**
 * Check asserting that an [Event] did **not** fire within a given channel category.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Category snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.notInCategory(id: Snowflake) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notInCategory")
	val category = event.kord.getChannelOf<Category>(id)

	if (category == null) {
		logger.noCategoryId(id)

		pass()
	} else {
		notInCategory { category }
	}
}

/**
 * Check asserting that the channel an [Event] fired in is above the given channel in the channel list.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Snowflake representing the ID of the channel to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.channelHigherThan(id: Snowflake) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.channelHigher")
	val channel = event.kord.getChannel(id)?.asChannelOfOrNull<GuildChannel>()

	if (channel == null) {
		logger.noChannelId(id)

		fail()
	} else {
		channelAbove { channel }
	}
}

/**
 * Check asserting that the channel an [Event] fired in is below the given channel in the channel list.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Snowflake representing the ID of the channel to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.channelLowerThan(id: Snowflake) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.channelLower")
	val channel = event.kord.getChannel(id)?.asChannelOfOrNull<GuildChannel>()

	if (channel == null) {
		logger.noChannelId(id)

		fail()
	} else {
		channelBelow { channel }
	}
}

// endregion
