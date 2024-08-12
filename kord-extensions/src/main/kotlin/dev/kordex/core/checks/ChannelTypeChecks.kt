/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.checks

import dev.kord.common.entity.ChannelType
import dev.kord.core.event.Event
import dev.kordex.core.checks.types.CheckContext
import dev.kordex.core.utils.translate
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Check asserting that the channel an [Event] fired in is of a given set of types.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channelTypes The channel types to compare to.
 */
public suspend fun CheckContext<*>.channelType(vararg channelTypes: ChannelType) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.channelType")
	val eventChannel = channelFor(event)

	if (eventChannel == null) {
		logger.nullChannel(event)

		fail()
	} else {
		val type = eventChannel.asChannel().type

		if (channelTypes.contains(type)) {
			logger.passed()

			pass()
		} else {
			logger.failed("Types $type is not within $channelTypes")

			fail(
				translate(
					"checks.channelType.failed",
					replacements = arrayOf(type.translate(locale)),
				)
			)
		}
	}
}

/**
 * Check asserting that the channel an [Event] fired in is **not** of a given set of types.
 *
 * Only events that can reasonably be associated with a single channel are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param channelTypes The channel types to compare to.
 */
public suspend fun CheckContext<*>.notChannelType(vararg channelTypes: ChannelType) {
	if (!passed) {
		return
	}

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notChannelType")
	val eventChannel = channelFor(event)

	if (eventChannel == null) {
		logger.nullChannel(event)

		pass()
	} else {
		val type = eventChannel.asChannel().type

		if (channelTypes.contains(type)) {
			logger.failed("Types $type is within $channelTypes")

			fail(
				translate(
					"checks.notChannelType.failed",
					replacements = arrayOf(type.translate(locale)),
				)
			)
		} else {
			logger.passed()

			pass()
		}
	}
}
