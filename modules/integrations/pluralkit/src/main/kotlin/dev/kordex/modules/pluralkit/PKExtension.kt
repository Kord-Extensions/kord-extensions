/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
import dev.kordex.core.i18n.EMPTY_VALUE_STRING
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
import dev.kordex.modules.pluralkit.i18n.generated.PluralKitTranslations
import dev.kordex.modules.pluralkit.storage.PKGuildConfig
import dev.kordex.modules.pluralkit.utils.LRUHashMap
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlin.text.split
import kotlin.time.Duration.Companion.seconds

const val NEGATIVE_EMOTE = "❌"
const val POSITIVE_EMOTE = "✅"

@Suppress("StringLiteralDuplication")
class PKExtension(val config: PKConfigBuilder) : Extension() {
	override val name: String = "ext-pluralkit"

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
			name = PluralKitTranslations.Command.Pluralkit.name
			description = PluralKitTranslations.Command.Pluralkit.description

			check { anyGuild() }

			ephemeralSubCommand(::ApiUrlArgs) {
				name = PluralKitTranslations.Command.Pluralkit.ApiUrl.name
				description = PluralKitTranslations.Command.Pluralkit.ApiUrl.description

				check { hasPermission(Permission.ManageGuild) }

				action {
					val guild = getGuild()!!
					val config = guild.config()
					val configUnit = guild.configUnit()

					if (arguments.url == null) {
						respond {
							content = PluralKitTranslations.Command.Pluralkit.ApiUrl.Response.current
								.translateLocale(getLocale(), config.apiUrl)
						}

						return@action
					}

					val translatedResetWords = PluralKitTranslations.Arguments.reset.translateLocale(getLocale())

					val resetWords = arrayOf(
						"reset",
					) + if (translatedResetWords != EMPTY_VALUE_STRING) {
						translatedResetWords.split(",")
					} else {
						listOf<String>()
					}

					if (arguments.url in resetWords) {
						config.apiUrl = PKGuildConfig().apiUrl
						configUnit.save(config)

						respond {
							content = PluralKitTranslations.Command.Pluralkit.ApiUrl.Response.reset
								.translateLocale(getLocale(), config.apiUrl)
						}

						return@action
					}

					config.apiUrl = arguments.url!!
					configUnit.save(config)

					respond {
						content = PluralKitTranslations.Command.Pluralkit.ApiUrl.Response.updated
							.translateLocale(getLocale(), config.apiUrl)
					}
				}
			}

			ephemeralSubCommand(::BotArgs) {
				name = PluralKitTranslations.Command.Pluralkit.Bot.name
				description = PluralKitTranslations.Command.Pluralkit.Bot.description

				check { hasPermission(Permission.ManageGuild) }

				action {
					val guild = getGuild()!!
					val config = guild.config()
					val configUnit = guild.configUnit()

					if (arguments.bot == null) {
						respond {
							content = PluralKitTranslations.Command.Pluralkit.Bot.Response.current
								.translateLocale(getLocale(), "<@${config.botId}>")
						}

						return@action
					}

					config.botId = arguments.bot!!.id
					configUnit.save(config)

					respond {
						content = PluralKitTranslations.Command.Pluralkit.Bot.Response.updated
							.translateLocale(getLocale(), "<@${config.botId}>")
					}
				}
			}

			ephemeralSubCommand {
				name = PluralKitTranslations.Command.Pluralkit.Status.name
				description = PluralKitTranslations.Command.Pluralkit.Status.description

				action {
					val config = guild!!.asGuild().config()

					respond {
						embed {
							title = PluralKitTranslations.Command.Pluralkit.Status.Response.title
								.translateLocale(getLocale())

							description = PluralKitTranslations.Command.Pluralkit.Status.Response.description
								.translateLocale(
									getLocale(),

									config.apiUrl,
									"<@${config.botId}>",
									config.enabled.emote(),
								)
						}
					}
				}
			}

			ephemeralSubCommand(::ToggleSupportArgs) {
				name = PluralKitTranslations.Command.Pluralkit.ToggleSupport.name
				description = PluralKitTranslations.Command.Pluralkit.ToggleSupport.description

				check { hasPermission(Permission.ManageGuild) }

				action {
					val guild = getGuild()!!
					val config = guild.config()
					val configUnit = guild.configUnit()

					if (arguments.toggle == null) {
						respond {
							content = PluralKitTranslations.Command.Pluralkit.ToggleSupport.Response.current
								.translateLocale(getLocale(), config.enabled.emote())
						}

						return@action
					}

					config.enabled = arguments.toggle!!
					configUnit.save(config)

					respond {
						content = PluralKitTranslations.Command.Pluralkit.ToggleSupport.Response.updated
							.translateLocale(getLocale(), config.enabled.emote())
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
			POSITIVE_EMOTE
		} else {
			NEGATIVE_EMOTE
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
			name = PluralKitTranslations.Argument.ApiUrl.name
			description = PluralKitTranslations.Argument.ApiUrl.description
		}
	}

	inner class BotArgs : Arguments() {
		val bot by optionalUser {
			name = PluralKitTranslations.Argument.Bot.name
			description = PluralKitTranslations.Argument.Bot.description
		}
	}

	inner class ToggleSupportArgs : Arguments() {
		val toggle by optionalBoolean {
			name = PluralKitTranslations.Argument.Toggle.name
			description = PluralKitTranslations.Argument.Toggle.description
		}
	}
}
