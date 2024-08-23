/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.welcome

import dev.kord.common.entity.DiscordComponent
import dev.kord.common.entity.EmbedType
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.component.Component
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kordex.core.builders.AboutBuilder
import dev.kordex.core.builders.ExtensionsBuilder
import dev.kordex.core.builders.about.CopyrightType
import dev.kordex.core.utils.loadModule
import dev.kordex.modules.func.welcome.config.SimpleWelcomeChannelConfig
import dev.kordex.modules.func.welcome.config.WelcomeChannelConfig
import dev.kordex.modules.func.welcome.data.WelcomeChannelData
import org.koin.dsl.bind

private const val DISCORD_CHANNEL_URI = "https://discord.com/channels"
private var copyrightAdded = false

internal fun AboutBuilder.addCopyright() {
	if (!copyrightAdded) {
		copyright(
			"kaml",
			"Apache-2.0",
			CopyrightType.Library,
			"https://github.com/charleskorn/kaml"
		)
	}

	copyrightAdded = true
}

fun ExtensionsBuilder.welcomeChannel(
	config: WelcomeChannelConfig,
	data: WelcomeChannelData,
) {
	loadModule { single { config } bind WelcomeChannelConfig::class }
	loadModule { single { data } bind WelcomeChannelData::class }

	add { WelcomeExtension() }
}

fun ExtensionsBuilder.welcomeChannel(
	data: WelcomeChannelData,
	body: SimpleWelcomeChannelConfig.Builder.() -> Unit,
) {
	welcomeChannel(SimpleWelcomeChannelConfig(body), data)
}

inline fun <reified T, reified R> List<T>.ifNotEmpty(body: (Collection<T>).() -> List<R>): List<R> {
	if (this.isNotEmpty()) {
		return body()
	}

	return emptyList()
}

fun MessageCreateBuilder.isSimilar(other: Message): Boolean {
	val builderComponents = components
		?.mapNotNull { it.build().components.value }
		?.ifNotEmpty {
			reduce { left, right -> left + right }
		} ?: emptyList()

	val messageComponents = other.actionRows
		.map { it.components }
		.ifNotEmpty {
			reduce { left, right -> left + right }
		}

	val messageEmbedBuilders = other.embeds
		.filter { it.type == null || it.type == EmbedType.Rich }
		.map { embed ->
			EmbedBuilder().also {
				embed.apply(it)
			}
		}

	if (content == null) {
		content = ""
	}

	return content == other.content &&
		embeds?.size == messageEmbedBuilders.size &&
		componentsAreSimilar(builderComponents, messageComponents) &&

		embeds?.filterIndexed { index, embed ->
			val otherEmbed = messageEmbedBuilders[index]

			embed.isSimilar(otherEmbed)
		}?.size == embeds?.size
}

fun componentsAreSimilar(
	builderComponents: List<DiscordComponent>,
	messageComponents: List<Component>,
): Boolean {
	if (builderComponents.size != messageComponents.size) {
		return false
	}

	if (builderComponents.isEmpty()) {
		return true
	}

	val results: MutableList<Boolean> = mutableListOf()

	builderComponents.forEachIndexed { index, builderComponent ->
		val messageComponent = messageComponents[index]

		results.add(
			builderComponent.customId == messageComponent.data.customId &&
				builderComponent.type == messageComponent.type &&
				builderComponent.label == messageComponent.data.label &&
				builderComponent.emoji == messageComponent.data.emoji &&
				builderComponent.disabled == messageComponent.data.disabled &&
				builderComponent.url == messageComponent.data.url
		)
	}

	return results.all { it }
}

fun EmbedBuilder.isSimilar(other: EmbedBuilder): Boolean {
	return title?.trim() == other.title?.trim() &&
		description?.trim() == other.description?.trim() &&
		footer?.text?.trim() == other.footer?.text?.trim() &&
		footer?.icon?.trim() == other.footer?.icon?.trim() &&
		image?.trim() == other.image?.trim() &&
		thumbnail?.url?.trim() == other.thumbnail?.url?.trim() &&
		author?.icon?.trim() == other.author?.icon?.trim() &&
		author?.url?.trim() == other.author?.url?.trim() &&
		author?.name?.trim() == other.author?.name?.trim() &&

		color == other.color &&
		timestamp == other.timestamp &&

		fields.all { field ->
			other.fields.any { otherField ->
				field.inline == otherField.inline &&
					field.value.trim() == otherField.value.trim() &&
					field.name.trim() == otherField.name.trim()
			}
		}
}

/**
 * Generate the jump URL for this channel.
 *
 * @return A clickable URL to jump to this channel.
 */
fun Channel.getJumpUrl(): String =
	"$DISCORD_CHANNEL_URI/${data.guildId.value?.value ?: "@me"}/${id.value}"
