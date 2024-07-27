/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
