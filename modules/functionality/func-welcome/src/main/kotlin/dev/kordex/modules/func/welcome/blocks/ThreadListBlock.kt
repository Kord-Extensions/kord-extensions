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
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.embed
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.time.TimestampType
import dev.kordex.core.time.toDiscord
import dev.kordex.modules.func.welcome.enums.ThreadListType
import dev.kordex.modules.func.welcome.getJumpUrl
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("DataClassContainsFunctions")
@Serializable
@SerialName("threads")
data class ThreadListBlock(
	val mode: ThreadListType,
	val limit: Int = 10,

	val text: String? = null,
	val description: String? = null,
	val color: Color = DISCORD_BLURPLE,
	val title: String = "${mode.humanReadable} Threads",
	val template: String = "**»** [{NAME}]({URL})",

	@SerialName("active_emoji")
	val activeEmoji: String? = null,

	@SerialName("archived_emoji")
	val archivedEmoji: String? = null,

	@SerialName("archive_status_in_name")
	val archiveStatusInName: Boolean = true,

	@SerialName("include_archived")
	val includeArchived: Boolean = true,

	@SerialName("include_news")
	val includeNews: Boolean = true,

	@SerialName("include_public")
	val includePublic: Boolean = true,

	@SerialName("include_private")
	val includePrivate: Boolean = false,

	@SerialName("include_hidden")
	val includeHidden: Boolean = false,

	@SerialName("include_hidden_channels")
	val includeHiddenChannels: Boolean = false,
) : Block() {
	override suspend fun create(builder: MessageCreateBuilder) {
		builder.content = text

		builder.embed { applyThreads() }
	}

	override suspend fun edit(builder: MessageModifyBuilder) {
		builder.content = text
		builder.components = mutableListOf()

		builder.embed { applyThreads() }
	}

	private suspend fun EmbedBuilder.applyThreads() {
		val threads = getThreads()

		this.color = this@ThreadListBlock.color
		this.title = this@ThreadListBlock.title

		description = buildString {
			if (this@ThreadListBlock.description != null) {
				append(this@ThreadListBlock.description)
				append("\n\n")
			}

			threads.forEach { thread ->
				var line = template
					.replace("{MENTION}", thread.mention)
					.replace("{URL}", thread.getJumpUrl())
					.replace("{CREATED_TIME}", thread.id.timestamp.toDiscord(TimestampType.RelativeTime))
					.replace("{PARENT_ID}", thread.parentId.toString())
					.replace("{PARENT}", thread.parent.mention)

				if (thread.lastMessageId != null) {
					line = line.replace(
						"{ACTIVE_TIME}",
						thread.lastMessageId!!.timestamp.toDiscord(TimestampType.RelativeTime)
					)
				}

				line = if (archiveStatusInName && thread.isArchived) {
					line.replace("{NAME}", thread.name + " (Archived)")
				} else {
					line.replace("{NAME}", thread.name)
				}

				line = when {
					thread.isArchived && archivedEmoji != null -> line.replace("{EMOJI}", archivedEmoji)
					thread.isArchived.not() && activeEmoji != null -> line.replace("{EMOJI}", activeEmoji)

					else -> line.replace("{EMOJI}", "")
				}

				appendLine(line)
			}
		}
	}

	private suspend fun getThreads(): List<ThreadChannel> {
		var threads = guild.cachedThreads
			.filter { thread ->
				if (!includeHiddenChannels) {
					val channel = thread.parent.asChannelOfOrNull<TextChannel>()

					if (channel == null) {
						false
					} else {
						val overwrite = channel.permissionOverwrites.firstOrNull { it.target == guild.id }

						overwrite == null || overwrite.denied.contains(Permission.ViewChannel).not()
					}
				} else {
					true
				}
			}
			.toList()

		threads = when (mode) {
			ThreadListType.ACTIVE -> threads.sortedByDescending { it.lastMessage?.id?.timestamp }
			ThreadListType.NEWEST -> threads.sortedByDescending { it.id.timestamp }
		}

		if (!includeArchived) {
			threads = threads.filter { !it.isArchived }
		}

		if (!includeNews) {
			threads = threads.filter { it.type != ChannelType.PublicNewsThread }
		}

		if (!includePublic) {
			threads = threads.filter { it.type != ChannelType.PublicGuildThread }
		}

		if (!includePrivate) {
			threads = threads.filter { it.type != ChannelType.PrivateThread }
		}

		if (!includeHidden) {
			threads = threads.filter {
				it.getParent()
					.getPermissionOverwritesForRole(it.guildId)
					?.denied
					?.contains(Permission.ViewChannel) != true
			}
		}

		return threads.take(limit)
	}
}
