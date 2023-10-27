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

package com.kotlindiscord.kord.extensions.modules.extra.pluralkit

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.checks.topChannelFor
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalUser
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.api.PluralKit
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.events.proxied
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.events.unproxied
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.storage.PKGuildConfig
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.utils.LRUHashMap
import com.kotlindiscord.kord.extensions.storage.StorageType
import com.kotlindiscord.kord.extensions.storage.StorageUnit
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.request.KtorRequestException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

const val NEGATIVE_EMOTE = "❌"
const val POSITIVE_EMOTE = "✅"

@Suppress("StringLiteralDuplication")
class PKExtension : Extension() {
    override val name: String = "ext-pluralkit"
    override val bundle: String = "kordex.pluralkit"

    private val logger = KotlinLogging.logger(
        "com.kotlindiscord.kord.extensions.modules.extra.pluralkit.PKExtension"
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

    private val apiMap: MutableMap<String, PluralKit> = mutableMapOf()

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
                val webhookId = message.data.webhookId.value

                if (webhookId == null) {
                    eventLock.withLock {
                        awaitingEvents[message.id] = event
                    }

                    val referencedMessage = message.messageReference?.message?.asMessageOrNull()

                    if (referencedMessage != null) {
                        replyCache[message.id] = referencedMessage
                    }

                    return@action
                }

                // This is to work around Kord's lack of support for forum channels. This can go once they're supported.
                val channel = try {
                    topChannelFor(event)?.asChannelOfOrNull<TopGuildMessageChannel>()
                } catch (e: ClassCastException) {
                    logger.warn(e) { "Failed to cast channel to TopGuildMessageChannel" }

                    null
                }

                val webhook = try {
                    channel
                        ?.asChannelOfOrNull<TopGuildMessageChannel>()
                        ?.webhooks
                        ?.firstOrNull { it.id == webhookId }
                } catch (e: KtorRequestException) {
                    logger.warn(e) { "Failed to retrieve webhooks for channel: ${channel?.id}" }

                    null
                }

                if (webhook == null || webhook.creatorId != config.botId) {
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
                val webhookId = message?.data?.webhookId?.value

                eventLock.withLock {
                    awaitingEvents.remove(message?.id)
                }

                if (webhookId == null) {
                    val pkMessage = guild.config().api().getMessageOrNull(event.messageId)

                    if (pkMessage != null) {
                        return@action
                    }

                    kord.launch {
                        bot.send(event.unproxied())
                    }

                    return@action
                }

                // This is to work around Kord's lack of support for forum channels. This can go once they're supported.
                val channel = try {
                    topChannelFor(event)?.asChannelOfOrNull<TopGuildMessageChannel>()
                } catch (e: ClassCastException) {
                    logger.warn(e) { "Failed to cast channel to TopGuildMessageChannel" }

                    null
                }

                val webhook = try {
                    channel
                        ?.asChannelOfOrNull<TopGuildMessageChannel>()
                        ?.webhooks
                        ?.firstOrNull { it.id == webhookId }
                } catch (e: KtorRequestException) {
                    logger.warn(e) { "Failed to retrieve webhooks for channel: ${channel?.id}" }

                    null
                }

                if (webhook == null || webhook.creatorId != config.botId) {
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
                val webhookId = message.asMessageOrNull()?.data?.webhookId?.value

                if (webhookId == null) {
                    kord.launch {
                        bot.send(event.unproxied())
                    }

                    return@action
                }

                // This is to work around Kord's lack of support for forum channels. This can go once they're supported.
                val channel = try {
                    topChannelFor(event)?.asChannelOfOrNull<TopGuildMessageChannel>()
                } catch (e: ClassCastException) {
                    logger.warn(e) { "Failed to cast channel to TopGuildMessageChannel" }

                    null
                }

                val webhook = try {
                    channel
                        ?.asChannelOfOrNull<TopGuildMessageChannel>()
                        ?.webhooks
                        ?.firstOrNull { it.id == webhookId }
                } catch (e: KtorRequestException) {
                    logger.warn(e) { "Failed to retrieve webhooks for channel: ${channel?.id}" }

                    null
                }

                if (webhook == null || webhook.creatorId != config.botId) {
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

    private fun PKGuildConfig.api(): PluralKit {
        var api = apiMap[apiUrl]

        if (api == null) {
            api = PluralKit(apiUrl)
            apiMap[apiUrl] = api
        }

        return api
    }

    private fun Boolean.emote() =
        if (this) {
            POSITIVE_EMOTE
        } else {
            NEGATIVE_EMOTE
        }

    private suspend fun GuildBehavior.configUnit() =
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
