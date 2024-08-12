/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.modules.func.welcome.blocks

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.embed
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.koin.KordExKoinComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Suppress("MagicNumber")
@Serializable
@SerialName("message_copy")
data class MessageCopyBlock(
	@SerialName("message_url")
	val messageUrl: String,

	val color: Color = DISCORD_BLURPLE,
	val template: String = "{TEXT}",
	val title: String? = null,

	@SerialName("use_embed")
	val useEmbed: Boolean = false,
) : Block(), KordExKoinComponent {
	val kord: Kord by inject()

	init {
		if ("{TEXT}" !in template) {
			error("Must provide a {TEXT} placeholder in the template")
		}
	}

	override suspend fun create(builder: MessageCreateBuilder) {
		val message = retrieveMessage(messageUrl)

		val content = template.replace("{TEXT}", message.content)

		if (useEmbed) {
			builder.embed {
				this@embed.color = this@MessageCopyBlock.color
				this@embed.description = content

				if (!this@MessageCopyBlock.title.isNullOrBlank()) {
					this@embed.title = this@MessageCopyBlock.title
				}
			}
		} else {
			builder.content = content
		}
	}

	override suspend fun edit(builder: MessageModifyBuilder) {
		val message = retrieveMessage(messageUrl)

		val content = template.replace("{TEXT}", message.content)

		if (useEmbed) {
			builder.embed {
				this@embed.color = this@MessageCopyBlock.color
				this@embed.description = content

				if (!this@MessageCopyBlock.title.isNullOrBlank()) {
					this@embed.title = this@MessageCopyBlock.title
				}
			}
		} else {
			builder.content = content
		}
	}
}

@Suppress("MagicNumber")
suspend fun MessageCopyBlock.retrieveMessage(url: String): Message {
	val ids = url.substringAfter("channels/")
		.split("/")
		.map { Snowflake(it) }

	val message = kord.getGuildOrNull(ids[0])
		?.getChannelOfOrNull<GuildMessageChannel>(ids[1])
		?.getMessageOrNull(ids[2])
		?: error("Unable to get message at URL: $messageUrl")

	if (message.getGuild().id != guild.id) {
		error("Message is not from the current server:  $messageUrl")
	}

	return message
}
