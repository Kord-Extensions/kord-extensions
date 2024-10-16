/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.modules.func.welcome

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlException
import dev.kord.common.asJavaLocale
import dev.kord.common.entity.MessageType
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.message.allowedMentions
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.builder.message.embed
import dev.kord.rest.request.RestRequestException
import dev.kordex.core.DISCORD_RED
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.utils.deleteIgnoringNotFound
import dev.kordex.core.utils.hasNotStatus
import dev.kordex.core.utils.scheduling.Scheduler
import dev.kordex.core.utils.scheduling.Task
import dev.kordex.modules.func.welcome.blocks.Block
import dev.kordex.modules.func.welcome.blocks.InteractionBlock
import dev.kordex.modules.func.welcome.config.WelcomeChannelConfig
import dev.kordex.modules.func.welcome.i18n.generated.WelcomeTranslations
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.decodeFromString
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject
import kotlin.collections.set

class WelcomeChannel(
	val channel: GuildMessageChannel,
	val url: String,
) : KordExKoinComponent {
	private var blocks: MutableList<Block> = mutableListOf()

	private val messageMapping: MutableMap<Snowflake, Block> = mutableMapOf()

	private val config: WelcomeChannelConfig by inject()
	private val client = HttpClient()

	private lateinit var yaml: Yaml
	private var task: Task? = null

	val scheduler: Scheduler = Scheduler()

	suspend fun handleInteraction(event: InteractionCreateEvent) {
		blocks.forEach {
			if (it is InteractionBlock) {
				it.handleInteraction(event)
			}
		}
	}

	suspend fun setup() {
		val taskDelay = config.getRefreshDelay()

		if (!::yaml.isInitialized) {
			yaml = Yaml(
				config.getSerializersModule(),
				YamlConfiguration(polymorphismStyle = PolymorphismStyle.Property)
			)
		}

		task?.cancel()

		if (taskDelay != null) {
			task = scheduler.schedule(taskDelay, false) {
				populate()
			}
		}

		populate()

		task?.start()
	}

	fun shutdown() {
		task?.cancel()
		scheduler.shutdown()
	}

	private suspend fun fetchBlocks(guild: Guild): List<Block> {
		try {
			val response = client.get(url).body<String>()

			return yaml.decodeFromString(response)
		} catch (e: ClientRequestException) {
				throw DiscordRelayedException(
					WelcomeTranslations.Error.downloadFailed
						.withLocale(guild.preferredLocale.asJavaLocale())
						.withNamedPlaceholders("error" to e.toString())
				)
		} catch (e: YamlException) {
			throw DiscordRelayedException(
				WelcomeTranslations.Error.downloadFailed
					.withLocale(guild.preferredLocale.asJavaLocale())
					.withNamedPlaceholders("error" to e.toString())
			)
		}
	}

	fun getBlocks(): List<Block> =
		blocks.toList()

	suspend fun populate() {
		task?.cancel()

		val guild = channel.getGuild()

		@Suppress("TooGenericExceptionCaught")
		try {
			blocks = fetchBlocks(guild).toMutableList()
		} catch (e: Exception) {
			log {
				embed {
					title = "Welcome channel update failed"
					color = DISCORD_RED

					description = buildString {
						appendLine("**__Failed to update blocks__**")
						appendLine()
						appendLine("```")
						appendLine(e)
						appendLine("```")
					}

					field {
						name = "Channel"
						value = "${channel.mention} (`${channel.id}` / `${channel.name}`)"
					}
				}
			}

			throw e
		}

		blocks.forEach {
			it.channel = channel
			it.guild = guild
		}

		val messages = channel.withStrategy(EntitySupplyStrategy.rest)
			.messages
			.filter { it.author?.id == channel.kord.selfId }
			.filter { it.type == MessageType.Default }
			.toList()
			.sortedBy { it.id.timestamp }

		@Suppress("TooGenericExceptionCaught")
		try {
			if (messages.size > blocks.size) {
				messages.forEachIndexed { index, message ->
					val block = blocks.getOrNull(index)

					if (block != null) {
						if (messageNeedsUpdate(message, block)) {
							message.edit {
								block.edit(this)

								allowedMentions { }
							}
						}

						messageMapping[message.id] = block
					} else {
						message.delete()
						messageMapping.remove(message.id)
					}
				}
			} else {
				blocks.forEachIndexed { index, block ->
					val message = messages.getOrNull(index)

					if (message != null) {
						if (messageNeedsUpdate(message, block)) {
							message.edit {
								block.edit(this)

								allowedMentions { }
							}
						}

						messageMapping[message.id] = block
					} else {
						val newMessage = channel.createMessage {
							block.create(this)

							allowedMentions { }
						}

						messageMapping[newMessage.id] = block
					}
				}
			}
		} catch (e: Exception) {
			log {
				embed {
					title = "Welcome channel update failed"
					color = DISCORD_RED

					description = buildString {
						appendLine("**__Failed to update messages__**")
						appendLine()
						appendLine("```")
						appendLine(e)
						appendLine("```")
					}

					field {
						name = "Channel"
						value = "${channel.mention} (`${channel.id}` / `${channel.name}`)"
					}
				}
			}

			throw e
		}

		task?.start()
	}

	suspend fun log(builder: suspend UserMessageCreateBuilder.() -> Unit): Message? =
		config.getLoggingChannel(channel, channel.guild.asGuild())?.createMessage { builder() }

	suspend fun clear() {
		val messages = channel.withStrategy(EntitySupplyStrategy.rest)
			.messages
			.toList()
			.filter { it.type == MessageType.Default }

		try {
			channel.bulkDelete(messages.map { it.id })
		} catch (e: RestRequestException) {
			if (e.hasNotStatus(HttpStatusCode.NotFound)) {
				@Suppress("TooGenericExceptionCaught")
				try {
					messages.forEach { it.deleteIgnoringNotFound() }
				} catch (e: Exception) {
					log {
						embed {
							title = "Failed to clear welcome channel"
							color = DISCORD_RED

							description = buildString {
								appendLine("**__Failed to clear channel__**")
								appendLine()
								appendLine("```")
								appendLine(e)
								appendLine("```")
							}

							field {
								name = "Channel"
								value = "${channel.mention} (`${channel.id}` / `${channel.name}`)"
							}
						}
					}

					throw e
				}
			}
		}
	}

	private suspend fun messageNeedsUpdate(message: Message, block: Block): Boolean {
		val builder = UserMessageCreateBuilder()

		block.create(builder)

		return !builder.isSimilar(message)
	}
}
