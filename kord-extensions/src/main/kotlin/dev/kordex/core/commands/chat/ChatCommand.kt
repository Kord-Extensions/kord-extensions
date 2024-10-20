/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication", "TooGenericExceptionCaught")

package dev.kordex.core.commands.chat

import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.ArgumentParsingException
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.annotations.ExtensionDSL
import dev.kordex.core.checks.types.*
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.Command
import dev.kordex.core.commands.events.*
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.impl.SENTRY_EXTENSION_NAME
import dev.kordex.core.i18n.EMPTY_VALUE_STRING
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
import dev.kordex.core.sentry.BreadcrumbType
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale
import dev.kordex.core.utils.respond
import dev.kordex.parser.StringParser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.get
import org.koin.core.component.inject
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Class representing a chat command.
 *
 * You shouldn't need to use this class directly - instead, create an [Extension] and use the
 * `chatCommand` function to register your command, by overriding the [Extension.setup]
 * function.
 *
 * @param extension The [Extension] that registered this command.
 * @param arguments Arguments object builder for this command, if it has arguments.
 */
@ExtensionDSL
public open class ChatCommand<T : Arguments>(
	extension: Extension,
	public open val arguments: (() -> T)? = null,
) : Command(extension) {

	/** Whether to allow the parser to parse keyword arguments. Defaults to `true`. **/
	public open var allowKeywordArguments: Boolean = true

	/**
	 * @suppress
	 */
	public open lateinit var body: suspend ChatCommandContext<out T>.() -> Unit

	/**
	 * A description of what this function and how it's intended to be used.
	 *
	 * This is intended to be made use of by help commands.
	 */
	public open var description: Key = CoreTranslations.Commands.defaultDescription

	/**
	 * Whether this command is enabled and can be invoked.
	 *
	 * Disabled commands cannot be invoked, and won't be shown in help commands.
	 *
	 * This can be changed at runtime, if commands need to be enabled and disabled dynamically without being
	 * reconstructed.
	 */
	public open var enabled: Boolean = true

	/**
	 * Whether to hide this command from help command listings.
	 *
	 * By default, this is `false` - so the command will be shown.
	 */
	public open var hidden: Boolean = false

	/**
	 * When translated, whether this command supports locale fallback when a user is trying to resolve a command by
	 * name in a locale other than the bot's configured default locale.
	 *
	 * If you'd like your command to be accessible in English along with the other languages the locale resolvers may
	 * have set up, turn this on.
	 */
	public open var localeFallback: Boolean = false

	/**
	 * Translation key referencing a comma-separated list of command aliases.
	 */
	public open var aliasKey: Key? = null

	/**
	 * @suppress
	 */
	public open val checkList: MutableList<ChatCommandCheck> = mutableListOf()

	/** Translation cache, so we don't have to look up translations every time. **/
	public open val aliasTranslationCache: MutableMap<Locale, Set<String>> = mutableMapOf()

	/** Provide a translation key here to replace the auto-generated signature string. **/
	public open var signature: Key? = null

	/** Locale-based cache of generated signature strings. **/
	public open var signatureCache: MutableMap<Locale, String> = mutableMapOf()

	/** Chat command registry. **/
	public val registry: ChatCommandRegistry by inject()

	/**
	 * Retrieve the command signature for a locale, which specifies how the command's arguments should be structured.
	 *
	 * Command signatures are generated automatically by the [ChatCommandParser].
	 */
	public open suspend fun getSignature(locale: Locale): String {
		if (this.arguments == null) {
			return ""
		}

		if (!signatureCache.containsKey(locale)) {
			if (signature != null) {
				signatureCache[locale] = signature!!
					.withLocale(locale)
					.translate()
			} else {
				signatureCache[locale] = registry.parser.signature(arguments!!, locale)
			}
		}

		return signatureCache[locale]!!
	}

	/** Return this command's name translated for the given locale, cached as required. **/
	public open fun getTranslatedName(locale: Locale): String {
		if (!nameTranslationCache.containsKey(locale)) {
			nameTranslationCache[locale] = name
				.withLocale(locale)
				.translate()
		}

		return nameTranslationCache[locale]!!
	}

	/** Return this command's aliases translated for the given locale, cached as required. **/
	public open fun getTranslatedAliases(locale: Locale): Set<String> {
		if (!aliasTranslationCache.containsKey(locale)) {
			if (aliasKey != null) {
				val translations = aliasKey!!
					.withLocale(locale)
					.translate()
					.lowercase()
					.split(",")
					.map { it.trim() }
					.filter { it != EMPTY_VALUE_STRING }
					.toSortedSet()

				aliasTranslationCache[locale] = translations
			} else {
				aliasTranslationCache[locale] = setOf()
			}
		}

		return aliasTranslationCache[locale]!!
	}

	/**
	 * An internal function used to ensure that all of a command's required arguments are present.
	 *
	 * @throws InvalidCommandException Thrown when a required argument hasn't been set.
	 */
	@Throws(InvalidCommandException::class)
	public override fun validate() {
		super.validate()

		if (!::body.isInitialized) {
			throw InvalidCommandException(name, "No command action given.")
		}
	}

	// region: DSL functions

	/**
	 * Define what will happen when your command is invoked.
	 *
	 * @param action The body of your command, which will be executed when your command is invoked.
	 */
	public open fun action(action: suspend ChatCommandContext<out T>.() -> Unit) {
		this.body = action
	}

	/**
	 * Define a check which must pass for the command to be executed.
	 *
	 * A command may have multiple checks - all checks must pass for the command to be executed.
	 * Checks will be run in the order that they're defined.
	 *
	 * This function can be used DSL-style with a given body, or it can be passed one or more
	 * predefined functions. See the samples for more information.
	 *
	 * @param checks Checks to apply to this command.
	 */
	public open fun check(vararg checks: ChatCommandCheck) {
		checks.forEach { checkList.add(it) }
	}

	/**
	 * Overloaded check function to allow for DSL syntax.
	 *
	 * @param check Check to apply to this command.
	 */
	public open fun check(check: ChatCommandCheck) {
		checkList.add(check)
	}

	// endregion

	/** Run checks with the provided [MessageCreateEvent]. Return false if any failed, true otherwise. **/
	public open suspend fun runChecks(
		event: MessageCreateEvent,
		sendMessage: Boolean = true,
		cache: MutableStringKeyedMap<Any>,
	): Boolean {
		val locale = event.getLocale()

		// global command checks
		for (check in extension.bot.settings.chatCommandsBuilder.checkList) {
			val context = CheckContextWithCache(event, locale, cache)

			check(context)

			if (!context.passed) {
				val message = context.getMessageKey()

				if (message != null && sendMessage) {
					event.message.respond {
						settings.failureResponseBuilder(
							this,
							message,

							FailureReason.ProvidedCheckFailure(
								DiscordRelayedException(
									message,
								)
							)
						)
					}
				}

				return false
			}
		}

		// local extension checks
		for (check in extension.chatCommandChecks) {
			val context = CheckContextWithCache(event, locale, cache)

			check(context)

			if (!context.passed) {
				val message = context.getMessageKey()

				if (message != null && sendMessage) {
					event.message.respond {
						settings.failureResponseBuilder(
							this,
							message,

							FailureReason.ProvidedCheckFailure(
								DiscordRelayedException(message)
							)
						)
					}
				}

				return false
			}
		}

		for (check in checkList) {
			val context = CheckContextWithCache(event, locale, cache)

			check(context)

			if (!context.passed) {
				val message = context.getMessageKey()

				if (message != null && sendMessage) {
					event.message.respond {
						settings.failureResponseBuilder(
							this,
							message,

							FailureReason.ProvidedCheckFailure(
								DiscordRelayedException(message)
							)
						)
					}
				}

				return false
			}
		}

		return true
	}

	/**
	 * Execute this command, given a [MessageCreateEvent].
	 *
	 * This function takes a [MessageCreateEvent] (generated when a message is received), and
	 * processes it. The command's checks are invoked and, assuming all of the
	 * checks passed, the [command body][action] is executed.
	 *
	 * If an exception is thrown by the [command body][action], it is caught and a traceback
	 * is printed.
	 *
	 * @param event The message creation event.
	 * @param commandName The name used to invoke this command.
	 * @param parser Parser used to parse the command's arguments, available for further parsing.
	 * @param argString Original string containing the command's arguments.
	 * @param skipChecks Whether to skip testing the command's checks.
	 */
	public open suspend fun call(
		event: MessageCreateEvent,
		commandName: String,
		parser: StringParser,
		argString: String,
		skipChecks: Boolean = false,
		cache: MutableStringKeyedMap<Any> = mutableMapOf(),
	): Unit = withLock {
		emitEventAsync(ChatCommandInvocationEvent(this, event))

		try {
			if (!skipChecks && !runChecks(event, cache = cache)) {
				emitEventAsync(
					ChatCommandFailedChecksEvent(
						this,
						event,

						CoreTranslations.Checks.failedWithoutMessage
							.withLocale(event.getLocale())
					)
				)

				return@withLock
			}
		} catch (e: DiscordRelayedException) {
			emitEventAsync(ChatCommandFailedChecksEvent(this, event, e.reason))

			event.message.respond {
				settings.failureResponseBuilder(
					this,
					e.reason.withLocale(event.getLocale()),
					FailureReason.ProvidedCheckFailure(e)
				)
			}

			return@withLock
		}

		val context = ChatCommandContext(this, event, commandName.toKey(event.getLocale()), parser, argString, cache)

		context.populate()

		if (sentry.enabled) {
			val translatedName = when (this) {
				is ChatSubCommand -> this.getFullTranslatedName(context.getLocale())
				is ChatGroupCommand -> this.getFullTranslatedName(context.getLocale())

				else -> this.getTranslatedName(context.getLocale())
			}

			context.sentry.context(
				"command",

				mapOf(
					"name" to translatedName,
					"type" to "chat",
					"extension" to extension.name,
				)
			)

			context.sentry.breadcrumb(BreadcrumbType.User) {
				category = "command.chat"
				message = "Command \"$name\" called."

				channel = event.message.getChannelOrNull()
				guild = event.message.getGuildOrNull()

				data["arguments"] = argString

				data["message.id"] = event.message.id.toString()
				data["message.content::arguments"] = event.message.content
			}
		}

		try {
			checkBotPerms(context)
		} catch (e: DiscordRelayedException) {
			event.message.respond {
				settings.failureResponseBuilder(
					this,
					e.reason.withLocale(context.getLocale()),
					FailureReason.OwnPermissionsCheckFailure(e)
				)
			}

			emitEventAsync(ChatCommandFailedChecksEvent(this, event, e.reason))

			return@withLock
		}

		if (this.arguments != null) {
			try {
				val parsedArgs = registry.parser.parse(this.arguments!!, context)
				context.populateArgs(parsedArgs)
			} catch (e: ArgumentParsingException) {
				event.message.respond {
					settings.failureResponseBuilder(
						this,
						e.reason.withLocale(context.getLocale()),
						FailureReason.ArgumentParsingFailure(e)
					)
				}

				emitEventAsync(ChatCommandFailedParsingEvent(this, event, e))

				return@withLock
			}
		}

		try {
			this.body(context)
		} catch (t: Throwable) {
			emitEventAsync(ChatCommandFailedWithExceptionEvent(this, event, t))

			if (t is DiscordRelayedException) {
				event.message.respond {
					settings.failureResponseBuilder(
						this,
						t.reason.withLocale(context.getLocale()),
						FailureReason.RelayedFailure(t)
					)
				}

				return@withLock
			}

			if (sentry.enabled) {
				logger.trace { "Submitting error to sentry." }

				val channel = event.message.getChannelOrNull()

				val sentryId = context.sentry.captureThrowable(t) {
					this.user = event.message.author
					this.channel = channel
				}

				logger.info { "Error submitted to Sentry: $sentryId" }

				sentry.addEventId(sentryId)

				logger.error(t) { "Error during execution of $name command ($event)" }

				if (extension.bot.extensions.containsKey(SENTRY_EXTENSION_NAME)) {
					val prefix = registry.getPrefix(event)

					event.message.respond {
						settings.failureResponseBuilder(
							this,

							CoreTranslations.Commands.Error.User.Sentry.message
								.withContext(context)
								.withOrdinalPlaceholders(
									prefix, sentryId
								),

							FailureReason.ExecutionError(t)
						)
					}
				} else {
					event.message.respond {
						settings.failureResponseBuilder(
							this,

							CoreTranslations.Commands.Error.user
								.withContext(context),

							FailureReason.ExecutionError(t)
						)
					}
				}
			} else {
				logger.error(t) { "Error during execution of $name command ($event)" }

				event.message.respond {
					settings.failureResponseBuilder(
						this,

						CoreTranslations.Commands.Error.user
							.withLocale(context.getLocale()),

						FailureReason.ExecutionError(t)
					)
				}
			}

			return@withLock
		}

		emitEventAsync(ChatCommandSucceededEvent(this, event))
	}
}
