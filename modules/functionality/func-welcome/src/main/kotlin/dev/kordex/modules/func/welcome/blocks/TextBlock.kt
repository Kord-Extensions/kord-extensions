/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.welcome.blocks

import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("text")
data class TextBlock(
	val text: String,
) : Block() {
	override suspend fun create(builder: MessageCreateBuilder) {
		builder.content = text
	}

	override suspend fun edit(builder: MessageModifyBuilder) {
		builder.content = text
		builder.embeds = mutableListOf()
		builder.components = mutableListOf()
	}
}
