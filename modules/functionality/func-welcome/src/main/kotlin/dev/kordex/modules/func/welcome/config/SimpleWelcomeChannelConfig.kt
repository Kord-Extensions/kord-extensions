/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.welcome.config

import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kordex.core.checks.types.Check
import dev.kordex.modules.func.welcome.blocks.Block
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlin.time.Duration

internal typealias LogChannelGetter = (suspend (channel: GuildMessageChannel, guild: Guild) -> GuildMessageChannel?)?

class SimpleWelcomeChannelConfig(private val builder: Builder) : WelcomeChannelConfig() {
	override val serializerBuilders: List<PolymorphicModuleBuilder<Block>.() -> Unit> =
		builder.serializerBuilders

	override suspend fun getLoggingChannel(channel: GuildMessageChannel, guild: Guild): GuildMessageChannel? =
		builder.loggingChannelGetter?.invoke(channel, guild)

	override suspend fun getStaffCommandChecks(): List<Check<*>> = builder.staffCommandChecks
	override suspend fun getRefreshDelay(): Duration? = builder.refreshDuration

	class Builder {
		internal val staffCommandChecks: MutableList<Check<*>> = mutableListOf()
		internal var loggingChannelGetter: LogChannelGetter = null

		val serializerBuilders: MutableList<PolymorphicModuleBuilder<Block>.() -> Unit> = mutableListOf()
		var refreshDuration: Duration? = null

		fun serializer(body: PolymorphicModuleBuilder<Block>.() -> Unit) {
			serializerBuilders.add(body)
		}

		fun staffCommandCheck(body: Check<*>) {
			staffCommandChecks.add(body)
		}

		fun getLogChannel(body: LogChannelGetter) {
			loggingChannelGetter = body
		}
	}
}

fun SimpleWelcomeChannelConfig(body: SimpleWelcomeChannelConfig.Builder.() -> Unit): SimpleWelcomeChannelConfig {
	val builder = SimpleWelcomeChannelConfig.Builder()

	body(builder)

	return SimpleWelcomeChannelConfig(builder)
}
