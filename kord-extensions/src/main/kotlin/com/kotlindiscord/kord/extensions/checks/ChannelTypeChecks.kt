/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.common.entity.ChannelType
import dev.kord.core.event.Event
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

	val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.channelType")
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

	val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notChannelType")
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
