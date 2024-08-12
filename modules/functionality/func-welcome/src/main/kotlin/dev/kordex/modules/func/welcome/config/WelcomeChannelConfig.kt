/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.welcome.config

import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kordex.core.checks.types.Check
import dev.kordex.modules.func.welcome.blocks.*
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.time.Duration

abstract class WelcomeChannelConfig {
	open val serializerBuilders: List<PolymorphicModuleBuilder<Block>.() -> Unit> = mutableListOf()

	val defaultSerializersModule: SerializersModule by lazy {
		SerializersModule {
			polymorphic(Block::class) {
				serializerBuilders.forEach { it() }

				subclass(ComplianceBlock::class)
				subclass(EmbedBlock::class)
				subclass(LinksBlock::class)
				subclass(MessageCopyBlock::class)
				subclass(RolesBlock::class)
				subclass(RulesBlock::class)
				subclass(TextBlock::class)
				subclass(ThreadListBlock::class)
			}
		}
	}

	/** Get the configured logging channel for the given channel and guild. **/
	abstract suspend fun getLoggingChannel(channel: GuildMessageChannel, guild: Guild): GuildMessageChannel?

	/**
	 * Get the configured staff command checks, used to ensure a staff-facing command can be run.
	 */
	abstract suspend fun getStaffCommandChecks(): List<Check<*>>

	abstract suspend fun getRefreshDelay(): Duration?

	/**
	 * Get the configured serializer module, which may be modified if other blocks have been set up.
	 */
	open suspend fun getSerializersModule(): SerializersModule = defaultSerializersModule
}
