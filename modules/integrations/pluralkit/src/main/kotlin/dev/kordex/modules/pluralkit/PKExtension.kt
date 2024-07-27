/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress(
	"MagicNumber",
	"UndocumentedPublicClass",
	"UndocumentedPublicFunction",
	"UndocumentedPublicProperty",
)

package dev.kordex.modules.pluralkit

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.rest.builder.message.embed
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.converters.impl.optionalBoolean
import dev.kordex.core.commands.converters.impl.optionalString
import dev.kordex.core.commands.converters.impl.optionalUser
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.event
import dev.kordex.core.storage.StorageType
import dev.kordex.core.storage.StorageUnit
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.kordExUserAgent
import dev.kordex.core.utils.scheduling.Scheduler
import dev.kordex.core.utils.scheduling.Task
import dev.kordex.modules.pluralkit.api.PluralKit
import dev.kordex.modules.pluralkit.config.PKConfigBuilder
import dev.kordex.modules.pluralkit.events.proxied
import dev.kordex.modules.pluralkit.events.unproxied
import dev.kordex.modules.pluralkit.storage.PKGuildConfig
import dev.kordex.modules.pluralkit.utils.LRUHashMap
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

const val NEGATIVE_EMOTE = "❌"
const val POSITIVE_EMOTE = "✅"

@Suppress("StringLiteralDuplication")
class PKExtension(val config: PKConfigBuilder) : Extension() {
	override val name: String = "ext-pluralkit"
	override val bundle: String = "kordex.pluralkit"

	private val logger = KotlinLogging.logger(
		"dev.kordex.modules.pluralkit.PKExtension"
	)

	private val guildConfig = StorageUnit(
		StorageType.Config,
		name,
		"guild-config",
		PKGuildConfig::class
	)

	private val eventLock = Mutex()

	private val awaitingEvents: MutableMap<Snowflake, MessageCreateEvent> = mutableMapOf()
	private val replyCache: LRUHashMap<Snowflake, Message> = LRUHashMap(1000)

	private val scheduler: Scheduler = Scheduler()
	private var checkTask: Task? = null

	private val apiMap: MutableStringKeyedMap<PluralKit> = mutableMapOf()

	override suspend fun setup() {
		checkTask = scheduler.schedule(
			1.seconds,
			true,
			"pk-check-task",
			1,
			repeat = true
		) {
			val now = Clock.System.now()
			val target = now - 3.seconds  // This delay will need fine-tuning over time

			eventLock.withLock {
				awaitingEvents
					.toMap()
					.filterKeys { it.timestamp < target }
					.forEach { (key, value) ->
						awaitingEvents.remove(key)
						replyCache.remove(key)

						kord.launch {
							bot.send(value.unproxied())
						}
					}
			}
		}

		event<MessageCreateEvent> {
			action {
				val guild = event.getGuildOrNull()

				if (guild == null) {
					kord.launch {
						bot.send(event.unproxied())
					}

					return@action
				}

				val config = guild.config()

				if (!config.enabled) {
					kord.launch {
						bot.send(event.unproxied())
					}

					return@action
				}

				val message = event.message
				val applicationId = message.data.applicationId.value

				if (applicationId != config.botId) {
					eventLock.withLock {
						awaitingEvents[message.id] = event
					}

					val referencedMessage = message.messageReference?.message?.asMessageOrNull()

					if (referencedMessage != null) {
						replyCache[message.id] = referencedMessage
					}

					return@action
				}

				delay(2.seconds)

				val pkMessage = config.api().getMessageOrNull(message.id)

				if (pkMessage == null) {
					return@action
				}

				eventLock.withLock {
					awaitingEvents.remove(pkMessage.original)
					awaitingEvents.remove(message.id)
				}

				kord.launch {
					bot.send(
						event.proxied(pkMessage, replyCache[message.id])
					)
				}
			}
		}

		event<MessageDeleteEvent> {
			action {
				val guild = event.getGuildOrNull()

				if (guild == null) {
					kord.launch {
						bot.send(event.unproxied())
					}

					return@action
				}

				val config = guild.config()

				if (!config.enabled) {
					kord.launch {
						bot.send(event.unproxied())
					}

					return@action
				}

				val message = event.message
				val applicationId = message?.data?.applicationId?.value

				eventLock.withLock {
					awaitingEvents.remove(message?.id)
				}

				if (applicationId != config.botId) {
					val pkMessage = guild.config().api().getMessageOrNull(event.messageId)

					if (pkMessage != null) {
						return@action
					}

					kord.launch {
						bot.send(event.unproxied())
					}

					return@action
				}

				delay(2.seconds)

				val pkMessage = config.api().getMessageOrNull(message.id)
					?: return@action

				kord.launch {
					bot.send(
						event.proxied(pkMessage, replyCache[message.id])
					)
				}
			}
		}

		event<MessageUpdateEvent> {
			action {
				val guild = event.channel.asChannelOfOrNull<GuildChannel>()?.getGuildOrNull()

				if (guild == null) {
					kord.launch {
						bot.send(event.unproxied())
					}

					return@action
				}

				val config = guild.config()

				if (!config.enabled) {
					kord.launch {
						bot.send(event.unproxied())
					}

					return@action
				}

				val message = event.message
				val applicationId = message.asMessageOrNull()?.data?.applicationId?.value

				if (applicationId != config.botId) {
					kord.launch {
						bot.send(event.unproxied())
					}

					return@action
				}

				delay(2.seconds)

				val pkMessage = config.api().getMessageOrNull(message.id)
					?: return@action

				kord.launch {
					bot.send(
						event.proxied(pkMessage, replyCache[message.id])
					)
				}
			}
		}

		ephemeralSlashCommand {
			name = "command.pluralkit.name"
			description = "command.pluralkit.description"

			check { anyGuild() }

			ephemeralSubCommand(::ApiUrlArgs) {
				name = "command.pluralkit.api-url.name"
				description = "command.pluralkit.api-url.description"

				check { hasPermission(Permission.ManageGuild) }

				action {
					val guild = getGuild()!!
					val config = guild.config()
					val configUnit = guild.configUnit()

					if (arguments.url == null) {
						respond {
							content = translate(
								"command.pluralkit.api-url.response.current",
								arrayOf(config.apiUrl)
							)
						}

						return@action
					}

					val resetWords = arrayOf(
						"reset",

						translate("arguments.reset"),

						translationsProvider.translate(
							"arguments.reset",
							bundleName = this@ephemeralSubCommand.bundle
						)
					)

					if (arguments.url in resetWords) {
						config.apiUrl = PKGuildConfig().apiUrl
						configUnit.save(config)

						respond {
							content = translate(
								"command.pluralkit.api-url.response.reset",
								arrayOf(config.apiUrl)
							)
						}

						return@action
					}

					config.apiUrl = arguments.url!!
					configUnit.save(config)

					respond {
						content = translate(
							"command.pluralkit.api-url.response.updated",
							arrayOf(config.apiUrl)
						)
					}
				}
			}

			ephemeralSubCommand(::BotArgs) {
				name = "command.pluralkit.bot.name"
				description = "command.pluralkit.bot.description"

				check { hasPermission(Permission.ManageGuild) }

				action {
					val guild = getGuild()!!
					val config = guild.config()
					val configUnit = guild.configUnit()

					if (arguments.bot == null) {
						respond {
							content = translate(
								"command.pluralkit.bot.response.current",
								arrayOf("<@${config.botId}>")
							)
						}

						return@action
					}

					config.botId = arguments.bot!!.id
					configUnit.save(config)

					respond {
						content = translate(
							"command.pluralkit.bot.response.updated",
							arrayOf("<@${config.botId}>")
						)
					}
				}
			}

			ephemeralSubCommand {
				name = "command.pluralkit.status.name"
				description = "command.pluralkit.status.description"

				action {
					val config = guild!!.asGuild().config()

					respond {
						embed {
							title = translate("command.pluralkit.status.response.title")

							description = translate(
								"command.pluralkit.status.response.description",

								arrayOf(
									config.apiUrl,
									"<@${config.botId}>",
									config.enabled.emote(),
								)
							)
						}
					}
				}
			}

			ephemeralSubCommand(::ToggleSupportArgs) {
				name = "command.pluralkit.toggle-support.name"
				description = "command.pluralkit.toggle-support.description"

				check { hasPermission(Permission.ManageGuild) }

				action {
					val guild = getGuild()!!
					val config = guild.config()
					val configUnit = guild.configUnit()

					if (arguments.toggle == null) {
						respond {
							content = translate(
								"command.pluralkit.toggle-support.response.current",
								arrayOf(config.enabled.emote())
							)
						}

						return@action
					}

					config.enabled = arguments.toggle!!
					configUnit.save(config)

					respond {
						content = translate(
							"command.pluralkit.toggle-support.response.updated",
							arrayOf(config.enabled.emote())
						)
					}
				}
			}
		}
	}

	override suspend fun unload() {
		checkTask?.cancel()
		checkTask = null
	}

	private suspend fun PKGuildConfig.api(): PluralKit =
		apiMap.getOrPut(apiUrl) {
			PluralKit(
				apiUrl,
				config.getLimiter(apiUrl),
				kord.kordExUserAgent(),
			)
		}

	private fun Boolean.emote() =
		if (this) {
			dev.kordex.modules.pluralkit.POSITIVE_EMOTE
		} else {
			dev.kordex.modules.pluralkit.NEGATIVE_EMOTE
		}

	private fun GuildBehavior.configUnit() =
		guildConfig.withGuild(id)

	private suspend fun GuildBehavior.config(): PKGuildConfig {
		val config = configUnit()

		return config.get()
			?: config.save(PKGuildConfig())
	}

	inner class ApiUrlArgs : Arguments() {
		val url by optionalString {
			name = "argument.api-url.name"
			description = "argument.api-url.description"
		}
	}

	inner class BotArgs : Arguments() {
		val bot by optionalUser {
			name = "argument.bot.name"
			description = "argument.bot.description"
		}
	}

	inner class ToggleSupportArgs : Arguments() {
		val toggle by optionalBoolean {
			name = "argument.toggle.name"
			description = "argument.toggle.description"
		}
	}
}
