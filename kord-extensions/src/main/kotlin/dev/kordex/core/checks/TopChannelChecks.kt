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
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.event.Event
import dev.kordex.core.annotations.NotTranslated
import dev.kordex.core.checks.types.CheckContext
import dev.kordex.core.i18n.generated.CoreTranslations
import io.github.oshai.kotlinlogging.KotlinLogging

// region: Entity DSL versions

/**
 * Check asserting that an [Event] fired within a given channel. If the event fired within a thread,
 * it checks the thread's parent channel instead.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the channel to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.inTopChannel(builder: suspend (T) -> ChannelBehavior) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.inChannel")
	val eventChannel = topChannelFor(event)

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
 * Check asserting that an [Event] did **not** fire within a given channel. If the event fired within a thread,
 * it checks the thread's parent channel instead.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the channel to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.notInTopChannel(builder: suspend (T) -> ChannelBehavior) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notInChannel")
	val eventChannel = topChannelFor(event)

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

// endregion

// region: Snowflake versions

/**
 * Check asserting that an [Event] fired within a given channel. If the event fired within a thread,
 * it checks the thread's parent channel instead.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Channel snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.inTopChannel(id: Snowflake) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.inChannel")
	var channel = event.kord.getChannel(id)

	if (channel is ThreadChannel) {
		channel = channel.parent.asChannel()
	}

	if (channel == null) {
		logger.noChannelId(id)

		fail()
	} else {
		inTopChannel { channel }
	}
}

/**
 * Check asserting that an [Event] did **not** fire within a given channel. If the event fired within a thread,
 * it checks the thread's parent channel instead.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Channel snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.notInTopChannel(id: Snowflake) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notInChannel")
	var channel = event.kord.getChannel(id)

	if (channel is ThreadChannel) {
		channel = channel.parent.asChannel()
	}

	if (channel == null) {
		logger.noChannelId(id)

		pass()
	} else {
		notInTopChannel { channel }
	}
}

// endregion
