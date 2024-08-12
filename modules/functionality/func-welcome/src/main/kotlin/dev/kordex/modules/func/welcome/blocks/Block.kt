/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.welcome.blocks

import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Suppress("UnnecessaryAbstractClass")
@Serializable
abstract class Block {
	@Transient
	lateinit var channel: GuildMessageChannel

	@Transient
	lateinit var guild: Guild

	abstract suspend fun create(builder: MessageCreateBuilder)
	abstract suspend fun edit(builder: MessageModifyBuilder)
}
