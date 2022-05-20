/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.modules.extra.phishing

import com.kotlindiscord.kord.extensions.DISCORD_RED
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
import dev.kord.core.behavior.ban
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.rest.builder.message.create.embed
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.UnsupportedMimeTypeException

/** The maximum number of redirects to attempt to follow for a URL. **/
const val MAX_REDIRECTS = 5

/** Phishing extension, responsible for checking for phishing domains in messages. **/
class PhishingExtension(private val settings: ExtPhishingBuilder) : Extension() {
    override val name = "phishing"

    private val api = PhishingApi(settings.appName)
    private val domainCache: MutableSet<String> = mutableSetOf()
    private val logger = KotlinLogging.logger { }

    private var websocket: PhishingWebsocketWrapper = api.websocket(::handleChange)

    private val httpClient = HttpClient {
        followRedirects = false
        expectSuccess = true
    }

    override suspend fun setup() {
        websocket.stop()
        websocket.start()

        domainCache.addAll(api.getAllDomains())

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
                    val matches = parseDomains(message.content)

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
        val matches = parseDomains(message.content)

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

    internal suspend fun parseDomains(content: String): MutableSet<String> {
        val domains: MutableSet<String> = mutableSetOf()

        for (match in settings.urlRegex.findAll(content)) {
            val found = match.groups[1]!!.value.trim('/')
            var domain = found

            if ("/" in domain) {
                domain = domain
                    .split("/", limit = 2)
                    .first()
                    .lowercase()
            }

            domain = domain.filter { it.isLetterOrDigit() || it in "-+&@#%?=~_|!:,.;" }

            if (domain in domainCache) {
                domains.add(domain)
            } else {
                val result = followRedirects(match.value)
                    ?.split("://")
                    ?.lastOrNull()
                    ?.split("/")
                    ?.first()
                    ?.lowercase()

                if (result in domainCache) {
                    domains.add(result!!)
                }
            }
        }

        logger.debug { "Found ${domains.size} domains: ${domains.joinToString()}" }

        return domains
    }

    @Suppress("MagicNumber")  // HTTP status codes
    internal suspend fun followRedirects(url: String, count: Int = 0): String? {
        if (count >= MAX_REDIRECTS) {
            logger.warn { "Maximum redirect count reached for URL: $url" }

            return url
        }

        val response: HttpResponse = try {
            httpClient.get(url)
        } catch (e: RedirectResponseException) {
            e.response
        } catch (e: ClientRequestException) {
            val status = e.response.status

            if (status.value !in 200 until 499) {
                logger.warn { "$url -> $status" }
            }

            return url
        }

        if (response.headers.contains("Location")) {
            val newUrl = response.headers["Location"]!!

            if (url.trim('/') == newUrl.trim('/')) {
                return null  // Results in the same URL
            }

            return followRedirects(newUrl, count + 1)
        } else {
            val soup = try {
                Jsoup.connect(url).get()
            } catch (e: UnsupportedMimeTypeException) {
                logger.debug { "$url -> Unsupported MIME type; not parsing" }

                return url
            }

            val element = soup.head()
                .getElementsByAttributeValue("http-equiv", "refresh")
                .first()

            if (element != null) {
                val content = element.attributes().get("content")

                val newUrl = content
                    .split(";")
                    .firstOrNull { it.startsWith("URL=", true) }
                    ?.split("=", limit = 2)
                    ?.lastOrNull()

                if (newUrl != null) {
                    if (url.trim('/') == newUrl.trim('/')) {
                        return null  // Results in the same URL
                    }

                    return followRedirects(newUrl, count + 1)
                }
            }
        }

        return url
    }

    override suspend fun unload() {
        websocket.stop()
    }

    internal fun handleChange(change: DomainChange) {
        when (change.type) {
            DomainChangeType.Add -> domainCache.addAll(change.domains)
            DomainChangeType.Delete -> domainCache.removeAll(change.domains)
        }
    }

    /** Arguments class for domain-relevant commands. **/
    inner class DomainArgs : Arguments() {
        /** Targeted domain string. **/
        val domain by string {
            name = "domain"
            description = "Domain to check"

            validate {
                failIf("Please provide the domain name only, without the protocol or a path.") { "/" in value }
            }
        }
    }
}
