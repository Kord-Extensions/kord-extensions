/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.modules.func.phishing

import dev.kord.core.behavior.ban
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.rest.builder.message.embed
import dev.kordex.core.DISCORD_RED
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.checks.isNotBot
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralMessageCommand
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.event
import dev.kordex.core.utils.dm
import dev.kordex.core.utils.getJumpUrl
import dev.kordex.core.utils.kordExUserAgent
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

/** The maximum number of redirects to attempt to follow for a URL. **/
const val MAX_REDIRECTS = 5

/** Phishing extension, responsible for checking for phishing domains in messages. **/
class PhishingExtension(private val settings: ExtPhishingBuilder) : Extension() {
	override val name = "kordex.func-phishing"

	private val domainCache: MutableSet<String> = settings.badDomains.toMutableSet()
	private val logger = KotlinLogging.logger { }

	private lateinit var api: PhishingApi
	private lateinit var websocket: PhishingWebsocketWrapper

	private val httpClient = HttpClient {
		followRedirects = false
		expectSuccess = true
	}

	override suspend fun setup() {
		api = PhishingApi(kord.kordExUserAgent())
		websocket = api.websocket(::handleChange)

		websocket.start()

		domainCache.addAll(api.getAllDomains())

		event<MessageCreateEvent> {
			check { isNotBot() }
			check { anyGuild() }

			check {
				settings.checks.forEach {
					if (passed) it()
				}
			}

			action {
				handleMessage(event.message.asMessageOrNull())
			}
		}

		event<MessageUpdateEvent> {
			check { isNotBot() }
			check { anyGuild() }

			check {
				settings.checks.forEach {
					if (passed) it()
				}
			}

			action {
				handleMessage(event.message.asMessageOrNull())
			}
		}

		ephemeralMessageCommand {
			name = "URL Safety Check"

			if (this@PhishingExtension.settings.requiredCommandPermission != null) {
				check { hasPermission(this@PhishingExtension.settings.requiredCommandPermission!!) }
			}

			action {
				for (message in targetMessages) {
					val matches = parseDomains(message.content)

					respond {
						content = if (matches.isNotEmpty()) {
							"⚠️ [Message ${message.id.value}](${message.getJumpUrl()}) " +
								"**contains ${matches.size} known unsafe link/s**."
						} else {
							"✅ [Message ${message.id.value}](${message.getJumpUrl()}) " +
								"**does not contain any known unsafe links**."
						}
					}
				}
			}
		}

		ephemeralSlashCommand(::DomainArgs) {
			name = "url-safety-check"
			description = "Check whether a given domain is a known unsafe domain."

			if (this@PhishingExtension.settings.requiredCommandPermission != null) {
				check { hasPermission(this@PhishingExtension.settings.requiredCommandPermission!!) }
			}

			action {
				respond {
					content = if (domainCache.contains(arguments.domain.lowercase())) {
						"⚠️ `${arguments.domain}` is a known unsafe domain."
					} else {
						"✅ `${arguments.domain}` is not a known unsafe domain."
					}
				}
			}
		}
	}

	private suspend fun handleMessage(message: Message?) {
		if (message == null) {
			return
		}

		val matches = parseDomains(message.content)

		if (matches.isNotEmpty()) {
			logger.debug { "Found a message with ${matches.size} unsafe domains." }

			if (settings.notifyUser) {
				message.kord.launch {
					message.author!!.dm {
						content = "We've detected that the following message contains an unsafe domain. For this " +
							"reason, **${settings.detectionAction.message}**."

						embed {
							title = "Unsafe domain detected"
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
								value = message.getGuildOrNull()?.name ?: "Unable to get guild"
							}
						}
					}
				}
			}

			when (settings.detectionAction) {
				DetectionAction.Ban -> {
					message.getAuthorAsMemberOrNull()!!.ban {
						reason = "Message linked to an unsafe domain"
					}

					message.delete("Message linked to an unsafe domain")
				}

				DetectionAction.Delete -> message.delete("Message linked to an unsafe domain")

				DetectionAction.Kick -> {
					message.getAuthorAsMemberOrNull()!!.kick("Message linked to an unsafe domain")
					message.delete("Message linked to an unsafe domain")
				}

				DetectionAction.LogOnly -> {
					// Do nothing, we always log
				}
			}

			logDeletion(message, matches)
		}
	}

	private suspend fun logDeletion(message: Message, matches: Set<String>) {
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

		val matchList = "# Unsafe Domain Matches\n\n" +
			"**Total:** ${matches.size}\n\n" +
			matches.joinToString("\n") { "* `$it`" }

		channel.createMessage {
			addFile(
				"matches.md",
				ChannelProvider { matchList.byteInputStream().toByteReadChannel() }
			)

			embed {
				title = "Unsafe domain detected"
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

	private suspend fun parseDomains(content: String): MutableSet<String> {
		val domains: MutableSet<String> = mutableSetOf()

		for (match in settings.urlRegex.findAll(content)) {
			val found = match.groups[1]?.value?.trim('/') ?: continue

			var domain = found

			if ("/" in domain) {
				domain = domain
					.split("/", limit = 2)
					.firstOrNull()
					?.lowercase() ?: continue
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

				if (result in domainCache && result != null) {
					domains.add(result)
				}
			}
		}

		logger.debug { "Found ${domains.size} domains: ${domains.joinToString()}" }

		return domains
	}

	@Suppress("MagicNumber", "TooGenericExceptionCaught")  // HTTP status codes
	private suspend fun followRedirects(url: String, count: Int = 0): String? {
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
		} catch (e: Exception) {
			logger.warn(e) { url }

			return url
		}

		if (response.headers.contains("Location")) {
			var newUrl = response.headers["Location"]!!

			if (newUrl.startsWith("/")) {
				val (protocol, path) = url.split("://", limit = 2)

				newUrl = "$protocol://${path.split("/", limit = 2).first()}$newUrl"
			} else if (newUrl.startsWith("./")) {
				newUrl = url.trimEnd('/') + newUrl.trimStart('.')
			}

			if (url.trim('/') == newUrl.trim('/')) {
				return null  // Results in the same URL
			}

			return followRedirects(newUrl, count + 1)
		} else {
			val soup = try {
				Jsoup.connect(url).get()
			} catch (e: Exception) {
				logger.debug(e) { e.message }

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

	private fun handleChange(change: DomainChange) {
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
