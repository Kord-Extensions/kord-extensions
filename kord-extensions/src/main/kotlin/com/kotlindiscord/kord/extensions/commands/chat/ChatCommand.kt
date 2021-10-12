@file:Suppress("StringLiteralDuplication", "TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.commands.chat

import com.kotlindiscord.kord.extensions.ArgumentParsingException
import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.events.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.i18n.EMPTY_VALUE_STRING
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.sentry.BreadcrumbType
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.utils.getLocale
import com.kotlindiscord.kord.extensions.utils.permissionsForMember
import com.kotlindiscord.kord.extensions.utils.respond
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.common.entity.Permission
import dev.kord.core.Kord
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
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
    public open val arguments: (() -> T)? = null
) : Command(extension), KoinComponent {
    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /** Bot settings object. **/
    public val settings: ExtensibleBotBuilder by inject()

    /** Message command registry. **/
    public val registry: ChatCommandRegistry by inject()

    /** Sentry adapter, for easy access to Sentry functions. **/
    public val sentry: SentryAdapter by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public val kord: Kord by inject()

    /**
     * @suppress
     */
    public open lateinit var body: suspend ChatCommandContext<out T>.() -> Unit

    /**
     * A description of what this function and how it's intended to be used.
     *
     * This is intended to be made use of by help commands.
     */
    public open var description: String = "commands.defaultDescription"

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
     * Alternative names that can be used to invoke your command.
     *
     * There's no limit on the number of aliases a command may have, but in the event of an alias matching
     * the [name] of a registered command, the command with the [name] takes priority.
     */
    public open var aliases: Array<String> = arrayOf()

    /**
     * Translation key referencing a comma-separated list of command aliases.
     *
     * If this is set, the [aliases] list is ignored. This is also slightly more efficient during the first
     * translation pass, as only one key will ever need to be translated.
     */
    public open var aliasKey: String? = null

    /**
     * @suppress
     */
    public open val checkList: MutableList<Check<MessageCreateEvent>> = mutableListOf()

    /** Permissions required to be able to run this command. **/
    public open val requiredPerms: MutableSet<Permission> = mutableSetOf()

    /** Translation cache, so we don't have to look up translations every time. **/
    public open val nameTranslationCache: MutableMap<Locale, String> = mutableMapOf()

    /** Translation cache, so we don't have to look up translations every time. **/
    public open val aliasTranslationCache: MutableMap<Locale, Set<String>> = mutableMapOf()

    /** Provide a translation key here to replace the auto-generated signature string. **/
    public open var signature: String? = null

    /** Locale-based cache of generated signature strings. **/
    public open var signatureCache: MutableMap<Locale, String> = mutableMapOf()

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
                signatureCache[locale] = translationsProvider.translate(
                    signature!!,
                    extension.bundle,
                    locale
                )
            } else {
                signatureCache[locale] = registry.parser.signature(arguments!!, locale)
            }
        }

        return signatureCache[locale]!!
    }

    /** Return this command's name translated for the given locale, cached as required. **/
    public open fun getTranslatedName(locale: Locale): String {
        if (!nameTranslationCache.containsKey(locale)) {
            nameTranslationCache[locale] = translationsProvider.translate(
                this.name,
                this.extension.bundle,
                locale
            ).lowercase()
        }

        return nameTranslationCache[locale]!!
    }

    /** Return this command's aliases translated for the given locale, cached as required. **/
    public open fun getTranslatedAliases(locale: Locale): Set<String> {
        if (!aliasTranslationCache.containsKey(locale)) {
            val translations = if (aliasKey != null) {
                translationsProvider.translate(aliasKey!!, extension.bundle, locale)
                    .lowercase()
                    .split(",")
                    .map { it.trim() }
                    .filter { it != EMPTY_VALUE_STRING }
                    .toSortedSet()
            } else {
                this.aliases.map {
                    translationsProvider.translate(it, extension.bundle, locale).lowercase()
                }.toSortedSet()
            }

            aliasTranslationCache[locale] = translations
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

    /** If your bot requires permissions to be able to execute the command, add them using this function. **/
    public fun requireBotPermissions(vararg perms: Permission) {
        perms.forEach { requiredPerms.add(it) }
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
    public open fun check(vararg checks: Check<MessageCreateEvent>) {
        checks.forEach { checkList.add(it) }
    }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to this command.
     */
    public open fun check(check: Check<MessageCreateEvent>) {
        checkList.add(check)
    }

    // endregion

    /** Run checks with the provided [MessageCreateEvent]. Return false if any failed, true otherwise. **/
    public open suspend fun runChecks(event: MessageCreateEvent, sendMessage: Boolean = true): Boolean {
        val locale = event.getLocale()

        // global command checks
        for (check in extension.bot.settings.chatCommandsBuilder.checkList) {
            val context = CheckContext(event, locale)

            check(context)

            if (!context.passed) {
                val message = context.getTranslatedMessage()

                if (message != null && sendMessage) {
                    event.message.respond {
                        settings.failureResponseBuilder(
                            this,
                            message,

                            FailureReason.ProvidedCheckFailure(
                                DiscordRelayedException(message, context.errorResponseKey)
                            )
                        )
                    }
                }

                return false
            }
        }

        // local extension checks
        for (check in extension.chatCommandChecks) {
            val context = CheckContext(event, locale)

            check(context)

            if (!context.passed) {
                val message = context.message

                if (message != null && sendMessage) {
                    event.message.respond {
                        settings.failureResponseBuilder(
                            this,
                            message,

                            FailureReason.ProvidedCheckFailure(
                                DiscordRelayedException(message, context.errorResponseKey)
                            )
                        )
                    }
                }

                return false
            }
        }

        for (check in checkList) {
            val context = CheckContext(event, locale)

            check(context)

            if (!context.passed) {
                val message = context.message

                if (message != null && sendMessage) {
                    event.message.respond {
                        settings.failureResponseBuilder(
                            this,
                            message,

                            FailureReason.ProvidedCheckFailure(
                                DiscordRelayedException(message, context.errorResponseKey)
                            )
                        )
                    }
                }

                return false
            }
        }

        return true
    }

    /** Checks whether the bot has the specified required permissions, throwing if it doesn't. **/
    @Throws(DiscordRelayedException::class)
    public open suspend fun checkBotPerms(context: ChatCommandContext<T>) {
        if (requiredPerms.isEmpty()) {
            return  // Nothing to check, don't try to hit the cache
        }

        if (context.guild != null) {
            val perms = (context.channel.asChannel() as GuildChannel)
                .permissionsForMember(kord.selfId)

            val missingPerms = requiredPerms.filter { !perms.contains(it) }

            if (missingPerms.isNotEmpty()) {
                throw DiscordRelayedException(
                    context.translate(
                        "commands.error.missingBotPermissions",
                        null,

                        replacements = arrayOf(
                            missingPerms.map { it.translate(context.getLocale()) }.joinToString(", ")
                        )
                    )
                )
            }
        }
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
        skipChecks: Boolean = false
    ): Unit = withLock {
        emitEventAsync(ChatCommandInvocationEvent(this, event))

        try {
            if (!skipChecks && !runChecks(event)) {
                emitEventAsync(
                    ChatCommandFailedChecksEvent(
                        this,
                        event,
                        "Checks failed without a message."
                    )
                )

                return@withLock
            }
        } catch (e: DiscordRelayedException) {
            emitEventAsync(ChatCommandFailedChecksEvent(this, event, e.reason))

            event.message.respond {
                settings.failureResponseBuilder(this, e.reason, FailureReason.ProvidedCheckFailure(e))
            }

            return@withLock
        }

        val context = ChatCommandContext(this, event, commandName, parser, argString)

        context.populate()

        if (sentry.enabled) {
            context.sentry.breadcrumb(BreadcrumbType.User) {
                category = "command.chat"
                message = "Command \"$name\" called."

                val channel = event.message.getChannelOrNull()
                val guild = event.message.getGuildOrNull()

                data["arguments"] = argString
                data["message"] = event.message.content

                if (channel != null) {
                    data["channel"] = when (channel) {
                        is DmChannel -> "Private Message (${channel.id.asString})"
                        is GuildMessageChannel -> "#${channel.name} (${channel.id.asString})"

                        else -> channel.id.asString
                    }
                }

                if (guild != null) {
                    data["guild"] = "${guild.name} (${guild.id.asString})"
                }
            }
        }

        try {
            checkBotPerms(context)
        } catch (e: DiscordRelayedException) {
            event.message.respond {
                settings.failureResponseBuilder(this, e.reason, FailureReason.OwnPermissionsCheckFailure(e))
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
                    settings.failureResponseBuilder(this, e.reason, FailureReason.ArgumentParsingFailure(e))
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
                    settings.failureResponseBuilder(this, t.reason, FailureReason.RelayedFailure(t))
                }

                return@withLock
            }

            if (sentry.enabled) {
                logger.trace { "Submitting error to sentry." }

                val channel = event.message.getChannelOrNull()

                val translatedName = when (this) {
                    is ChatSubCommand -> this.getFullTranslatedName(context.getLocale())
                    is ChatGroupCommand -> this.getFullTranslatedName(context.getLocale())

                    else -> this.getTranslatedName(context.getLocale())
                }

                val sentryId = context.sentry.captureException(t, "MessageCommand execution failed.") {
                    val author = event.message.author

                    if (author != null) {
                        user(author)
                    }

                    tag("private", "false")

                    if (channel is DmChannel) {
                        tag("private", "true")
                    }

                    tag("command", translatedName)
                    tag("extension", extension.name)
                }

                logger.info { "Error submitted to Sentry: $sentryId" }

                sentry.addEventId(sentryId)

                logger.error(t) { "Error during execution of $name command ($event)" }

                if (extension.bot.extensions.containsKey("sentry")) {
                    val prefix = registry.getPrefix(event)

                    event.message.respond {
                        settings.failureResponseBuilder(
                            this,

                            context.translate(
                                "commands.error.user.sentry.message",
                                null,
                                replacements = arrayOf(
                                    prefix,
                                    sentryId
                                )
                            ),

                            FailureReason.ExecutionError(t)
                        )
                    }
                } else {
                    event.message.respond {
                        settings.failureResponseBuilder(
                            this,
                            context.translate("commands.error.user", null),
                            FailureReason.ExecutionError(t)
                        )
                    }
                }
            } else {
                logger.error(t) { "Error during execution of $name command ($event)" }

                event.message.respond {
                    settings.failureResponseBuilder(
                        this,
                        context.translate("commands.error.user", null),
                        FailureReason.ExecutionError(t)
                    )
                }
            }

            return@withLock
        }

        emitEventAsync(ChatCommandSucceededEvent(this, event))
    }
}
