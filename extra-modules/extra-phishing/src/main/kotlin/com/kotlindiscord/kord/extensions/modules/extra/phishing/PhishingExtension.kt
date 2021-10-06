@file:OptIn(ExperimentalTime::class)
@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.modules.extra.phishing

import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.dm
import com.kotlindiscord.kord.extensions.utils.getJumpUrl
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.core.behavior.ban
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.time.ExperimentalTime

/** Phishing extension, responsible for checking for phishing domains in messages. **/
class PhishingExtension(private val settings: ExtPhishingBuilder) : Extension() {
    override val name = "phishing"

    private val api = PhishingApi(settings.appName)
    private val domainCache: MutableSet<String> = mutableSetOf()
    private val logger = KotlinLogging.logger { }

    private val scheduler = Scheduler()
    private var checkTask: Task? = null

    override suspend fun setup() {
        domainCache.addAll(api.getAllDomains())

        checkTask = scheduler.schedule(settings.updateDelay, pollingSeconds = 30, callback = ::updateDomains)

        event<MessageCreateEvent> {
            check { isNotBot() }
            check { event.message.author != null }
            check { event.guildId != null }

            check {
                settings.checks.forEach {
                    if (passed) it()
                }
            }

            action {
                handleMessage(event.message)
            }
        }

        event<MessageUpdateEvent> {
            check { isNotBot() }
            check { event.new.author.value != null }
            check { event.new.guildId.value != null }

            check {
                settings.checks.forEach {
                    if (passed) it()
                }
            }

            action {
                handleMessage(event.message.asMessage())
            }
        }

        ephemeralMessageCommand {
            name = "Phishing Check"

            if (this@PhishingExtension.settings.requiredCommandPermission != null) {
                check { hasPermission(this@PhishingExtension.settings.requiredCommandPermission!!) }
            }

            action {
                for (message in targetMessages) {
                    val domains = parseDomains(message.content.lowercase())
                    val matches = domains intersect domainCache

                    respond {
                        content = if (matches.isNotEmpty()) {
                            "⚠️ [Message ${message.id.value}](${message.getJumpUrl()}) " +
                                "**contains ${matches.size} phishing link/s**."
                        } else {
                            "✅ [Message ${message.id.value}](${message.getJumpUrl()}) " +
                                "**does not contain any phishing links**."
                        }
                    }
                }
            }
        }

        ephemeralSlashCommand(::DomainArgs) {
            name = "phishing-check"
            description = "Check whether a given domain is a known phishing domain."

            if (this@PhishingExtension.settings.requiredCommandPermission != null) {
                check { hasPermission(this@PhishingExtension.settings.requiredCommandPermission!!) }
            }

            action {
                respond {
                    content = if (domainCache.contains(arguments.domain.lowercase())) {
                        "⚠️ `${arguments.domain}` is a known phishing domain."
                    } else {
                        "✅ `${arguments.domain}` is not a known phishing domain."
                    }
                }
            }
        }
    }

    internal suspend fun handleMessage(message: Message) {
        val domains = parseDomains(message.content.lowercase())
        val matches = domains intersect domainCache

        if (matches.isNotEmpty()) {
            logger.debug { "Found a message with ${matches.size} phishing domains." }

            if (settings.notifyUser) {
                message.kord.launch {
                    message.author!!.dm {
                        content = "We've detected that the following message contains a phishing domain. For this " +
                            "reason, **${settings.detectionAction.message}**."

                        embed {
                            title = "Phishing domain detected"
                            description = message.content
                            color = DISCORD_RED

                            field {
                                inline = true

                                name = "Channel"
                                value = message.channel.mention
                            }

                            field {
                                inline = true

                                name = "Message ID"
                                value = "`${message.id.value}`"
                            }

                            field {
                                inline = true

                                name = "Server"
                                value = message.getGuild().name
                            }
                        }
                    }
                }
            }

            when (settings.detectionAction) {
                DetectionAction.Ban -> {
                    message.getAuthorAsMember()!!.ban {
                        reason = "Message contained a phishing domain"
                    }

                    message.delete("Message contained a phishing domain")
                }

                DetectionAction.Delete -> message.delete("Message contained a phishing domain")

                DetectionAction.Kick -> {
                    message.getAuthorAsMember()!!.kick("Message contained a phishing domain")
                    message.delete("Message contained a phishing domain")
                }

                DetectionAction.LogOnly -> {
                    // Do nothing, we always log
                }
            }

            logDeletion(message, matches)
        }
    }

    internal suspend fun logDeletion(message: Message, matches: Set<String>) {
        val guild = message.getGuild()

        val channel = message
            .getGuild()
            .channels
            .filter { it.name == settings.logChannelName }
            .lastOrNull()
            ?.asChannelOrNull() as? GuildMessageChannel

        if (channel == null) {
            logger.warn {
                "Unable to find a channel named ${settings.logChannelName} on ${guild.name} (${guild.id.value})"
            }

            return
        }

        val matchList = "# Phishing Domain Matches\n\n" +
            "**Total:** ${matches.size}\n\n" +
            matches.joinToString("\n") { "* `$it`" }

        channel.createMessage {
            addFile("matches.md", matchList.byteInputStream())

            embed {
                title = "Phishing domain detected"
                description = message.content
                color = DISCORD_RED

                field {
                    inline = true

                    name = "Author"
                    value = "${message.author!!.mention} (" +
                        "`${message.author!!.tag}` / " +
                        "`${message.author!!.id.value}`" +
                        ")"
                }

                field {
                    inline = true

                    name = "Channel"
                    value = "${message.channel.mention} (`${message.channelId.value}`)"
                }

                field {
                    inline = true

                    name = "Message"
                    value = "[`${message.id.value}`](${message.getJumpUrl()})"
                }

                field {
                    inline = true

                    name = "Total Matches"
                    value = matches.size.toString()
                }
            }
        }
    }

    internal fun parseDomains(content: String): MutableSet<String> {
        val domains: MutableSet<String> = mutableSetOf()

        for (match in settings.urlRegex.findAll(content)) {
            var found = match.groups[1]!!.value.trim('/')

            if ("/" in found) {
                found = found.split("/", limit = 2).first()
            }

            domains.add(found)
        }

        logger.debug { "Found ${domains.size} domains: ${domains.joinToString()}" }

        return domains
    }

    override suspend fun unload() {
        checkTask?.cancel()
        checkTask = null
    }

    @Suppress("MagicNumber")
    internal suspend fun updateDomains() {
        logger.trace { "Updating domains..." }

        // An extra 30 seconds for safety
        domainCache.addAll(api.getRecentDomains(settings.updateDelay.inWholeSeconds + 30))
        checkTask?.restart()  // Off we go again
    }

    /** Arguments class for domain-relevant commands. **/
    inner class DomainArgs : Arguments() {
        /** Targeted domain string. **/
        val domain by string("domain", "Domain to check") { _, value ->
            if ("/" in value) {
                throw DiscordRelayedException("Please provide the domain name only, without the protocol or a path.")
            }
        }
    }
}
