/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.modules.func.phishing

import dev.kord.common.asJavaLocale
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
import dev.kordex.core.i18n.generated.CoreTranslations.Extensions.Help.Paginator.Title.arguments
import dev.kordex.core.utils.dm
import dev.kordex.core.utils.getJumpUrl
import dev.kordex.core.utils.kordExUserAgent
import dev.kordex.modules.func.phishing.i18n.generated.PhishingTranslations
import dev.kordex.modules.func.phishing.i18n.generated.PhishingTranslations.Actions.logMessage
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
import java.util.Locale

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

	init {
		bot.settings.aboutBuilder.addCopyright()
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
				handleMessage(event.message.asMessageOrNull(), getLocale())
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
				handleMessage(event.message.asMessageOrNull(), getLocale())
			}
		}

		ephemeralMessageCommand {
			name = PhishingTranslations.Commands.Message.name

			if (this@PhishingExtension.settings.requiredCommandPermission != null) {
				check { hasPermission(this@PhishingExtension.settings.requiredCommandPermission!!) }
			}

			action {
				for (message in targetMessages) {
					val matches = parseDomains(message.content)
					val locale = getLocale()

					respond {
						content = if (matches.isNotEmpty()) {
							PhishingTranslations.Response.Message.unsafe
								.translateNamedLocale(
									locale,

									"emoji" to locale.unsafeEmoji(),
									"id" to message.id.value,
									"url" to message.getJumpUrl(),
									"matches" to matches.size
								)
						} else {
							PhishingTranslations.Response.Message.safe
								.translateNamedLocale(
									locale,

									"emoji" to locale.safeEmoji(),
									"id" to message.id.value,
									"url" to message.getJumpUrl(),
								)
						}
					}
				}
			}
		}

		ephemeralSlashCommand(::DomainArgs) {
			name = PhishingTranslations.Commands.Slash.name
			description = PhishingTranslations.Commands.Slash.description

			if (this@PhishingExtension.settings.requiredCommandPermission != null) {
				check { hasPermission(this@PhishingExtension.settings.requiredCommandPermission!!) }
			}

			action {
				val locale = getLocale()

				respond {
					content = if (domainCache.contains(arguments.domain.lowercase())) {
						PhishingTranslations.Response.Domain.unsafe
							.translateNamedLocale(
								locale,

								"emoji" to locale.unsafeEmoji(),
								"domain" to arguments.domain,
							)
					} else {
						PhishingTranslations.Response.Domain.safe
							.translateNamedLocale(
								locale,

								"emoji" to locale.unsafeEmoji(),
								"domain" to arguments.domain,
							)
					}
				}
			}
		}
	}

	private suspend fun handleMessage(message: Message?, locale: Locale) {
		if (message == null) {
			return
		}

		val matches = parseDomains(message.content)

		if (matches.isNotEmpty()) {
			logger.debug { "Found a message with ${matches.size} unsafe domains." }

			if (settings.notifyUser) {
				message.kord.launch {
					message.author!!.dm {
						content = PhishingTranslations.Notice.Message.unsafe
							.translateNamedLocale(
								locale,

								"action" to settings.detectionAction.message.translateLocale(locale)
							)

						embed {
							title = PhishingTranslations.Embed.Logging.Unsafe.title.translateLocale(locale)
							description = message.content
							color = DISCORD_RED

							field {
								inline = true

								name = PhishingTranslations.Fields.channel.translateLocale(locale)
								value = message.channel.mention
							}

							field {
								inline = true

								name = PhishingTranslations.Fields.messageId.translateLocale(locale)
								value = "`${message.id.value}`"
							}

							field {
								inline = true

								name = PhishingTranslations.Fields.Server.name.translateLocale(locale)
								value = message.getGuildOrNull()?.name
									?: PhishingTranslations.Fields.Server.unknown.translateLocale(locale)
							}
						}
					}
				}
			}

			val translatedLogMessage = PhishingTranslations.Actions.logMessage
				.withLocale(message.getGuildOrNull()?.preferredLocale?.asJavaLocale())
				.translate()

			when (settings.detectionAction) {
				DetectionAction.Ban -> {
					message.getAuthorAsMemberOrNull()!!.ban {
						reason = translatedLogMessage
					}

					message.delete(translatedLogMessage)
				}

				DetectionAction.Delete -> message.delete(translatedLogMessage)

				DetectionAction.Kick -> {
					message.getAuthorAsMemberOrNull()!!.kick(translatedLogMessage)
					message.delete(translatedLogMessage)
				}

				DetectionAction.LogOnly -> {
					// Do nothing, we always log
				}
			}

			logDeletion(message, locale, matches)
		}
	}

	private suspend fun logDeletion(message: Message, locale: Locale, matches: Set<String>) {
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

		val matchList = PhishingTranslations.Response.Matches.header
			.translateNamedLocale(
				locale,

				"matches" to matches.size
			) + "\n\n" + matches.joinToString("\n") { "* `$it`" }

		channel.createMessage {
			addFile(
				PhishingTranslations.Response.Matches.filename.translateLocale(locale),
				ChannelProvider { matchList.byteInputStream().toByteReadChannel() }
			)

			embed {
				title = PhishingTranslations.Embed.Logging.Unsafe.title.translateLocale(locale)
				description = message.content
				color = DISCORD_RED

				field {
					inline = true

					name = PhishingTranslations.Fields.author.translateLocale(locale)
					value = "${message.author!!.mention} (" +
						"`${message.author!!.tag}` / " +
						"`${message.author!!.id.value}`" +
						")"
				}

				field {
					inline = true

					name = PhishingTranslations.Fields.channel.translateLocale(locale)
					value = "${message.channel.mention} (`${message.channelId.value}`)"
				}

				field {
					inline = true

					name = PhishingTranslations.Fields.message.translateLocale(locale)
					value = "[`${message.id.value}`](${message.getJumpUrl()})"
				}

				field {
					inline = true

					name = PhishingTranslations.Fields.totalMatches.translateLocale(locale)
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

	suspend fun Locale.safeEmoji() =
		PhishingTranslations.Response.Emoji.safe.translateLocale(this)

	suspend fun Locale.unsafeEmoji() =
		PhishingTranslations.Response.Emoji.unsafe.translateLocale(this)

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
			name = PhishingTranslations.Args.Domain.name
			description = PhishingTranslations.Args.Domain.description

			validate {
				failIf(PhishingTranslations.Args.Domain.validationError) { "/" in value }
			}
		}
	}
}
