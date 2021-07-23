@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.commands.cooldowns.base.CooldownProvider
import com.kotlindiscord.kord.extensions.commands.cooldowns.base.CooldownType
import com.kotlindiscord.kord.extensions.commands.cooldowns.base.MutableCooldownProvider
import com.kotlindiscord.kord.extensions.commands.parser.ArgumentParser
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.i18n.EMPTY_VALUE_STRING
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import com.kotlindiscord.kord.extensions.utils.getLocale
import com.kotlindiscord.kord.extensions.utils.respond
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.common.entity.Permission
import dev.kord.core.Kord
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

private val logger = KotlinLogging.logger {}

/**
 * Class representing a message command.
 *
 * You shouldn't need to use this class directly - instead, create an [Extension] and use the
 * [command function][Extension.command] to register your command, by overriding the [Extension.setup]
 * function.
 *
 * @param extension The [Extension] that registered this command.
 * @param arguments Arguments object builder for this command, if it has arguments.
 */
@OptIn(ExperimentalTime::class)
@ExtensionDSL
public open class MessageCommand<T : Arguments>(
    extension: Extension,
    public open val arguments: (() -> T)? = null
) : Command(extension), KoinComponent {
    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /** Message command registry. **/
    public val messageCommandsRegistry: MessageCommandRegistry by inject()

    private val settings: ExtensibleBotBuilder by inject()

    /** Sentry adapter, for easy access to Sentry functions. **/
    public val sentry: SentryAdapter by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public val kord: Kord by inject()

    /**
     * @suppress
     */
    public open lateinit var body: suspend MessageCommandContext<out T>.() -> Unit

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

    /**
     * @suppress
     */
    public open val cooldowns: CooldownProvider = settings.messageCommandsBuilder.cooldownsBuilder.provider()

    override val parser: ArgumentParser = ArgumentParser()

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

    /** Cooldown object that keeps track of the cooldowns for this command. **/
    public var cooldown: MutableCooldownProvider = settings.slashCommandsBuilder.cooldownsBuilder.provider()

    /** Cooldown body that defines the duration for the cooldown type. **/
    public var cooldownBody: suspend MessageCreateEvent.() -> Duration? = { null }

    /** The cooldown type that this command has. */
    public lateinit var cooldownTypeKClass: KClass<out CooldownType>

    /**
     * Retrieve the command signature for a locale, which specifies how the command's arguments should be structured.
     *
     * Command signatures are generated automatically by the [ArgumentParser].
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
                signatureCache[locale] = parser.signature(arguments!!, locale)
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
    public fun requirePermissions(vararg perms: Permission) {
        perms.forEach { requiredPerms.add(it) }
    }

    // region: DSL functions

    /**
     * Define what will happen when your command is invoked.
     *
     * @param action The body of your command, which will be executed when your command is invoked.
     */
    public open fun action(action: suspend MessageCommandContext<out T>.() -> Unit) {
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

    /**
     * Define a simple Boolean check which must pass for the command to be executed.
     *
     * Boolean checks are simple wrappers around the regular check system, allowing you to define a basic check that
     * takes an event object and returns a [Boolean] representing whether it passed. This style of check does not have
     * the same functionality as a regular check, and cannot return a message.
     *
     * A command may have multiple checks - all checks must pass for the command to be executed.
     * Checks will be run in the order that they're defined.
     *
     * This function can be used DSL-style with a given body, or it can be passed one or more
     * predefined functions. See the samples for more information.
     *
     * @param checks Checks to apply to this command.
     */
    public open fun booleanCheck(vararg checks: suspend (MessageCreateEvent) -> Boolean) {
        checks.forEach(::booleanCheck)
    }

    /**
     * Overloaded simple Boolean check function to allow for DSL syntax.
     *
     * Boolean checks are simple wrappers around the regular check system, allowing you to define a basic check that
     * takes an event object and returns a [Boolean] representing whether it passed. This style of check does not have
     * the same functionality as a regular check, and cannot return a message.
     *
     * @param check Check to apply to this command.
     */
    public open fun booleanCheck(check: suspend (MessageCreateEvent) -> Boolean) {
        check {
            if (check(event)) {
                pass()
            } else {
                fail()
            }
        }
    }

    // endregion

    /** Run checks with the provided [MessageCreateEvent]. Return false if any failed, true otherwise. **/
    public open suspend fun runChecks(event: MessageCreateEvent, sendMessage: Boolean = true): Boolean {
        val locale = event.getLocale()

        // global command checks
        for (check in extension.bot.settings.messageCommandsBuilder.checkList) {
            val context = CheckContext(event, locale)

            check(context)

            if (!context.passed) {
                val message = context.message

                if (message != null && sendMessage) {
                    event.message.respond(
                        translationsProvider.translate(
                            "checks.responseTemplate",
                            replacements = arrayOf(message)
                        )
                    )
                }

                return false
            }
        }

        // local extension checks
        for (check in extension.commandChecks) {
            val context = CheckContext(event, locale)

            check(context)

            if (!context.passed) {
                val message = context.message

                if (message != null && sendMessage) {
                    event.message.respond(
                        translationsProvider.translate(
                            "checks.responseTemplate",
                            replacements = arrayOf(message)
                        )
                    )
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
                    event.message.respond(
                        translationsProvider.translate(
                            "checks.responseTemplate",
                            replacements = arrayOf(message)
                        )
                    )
                }

                return false
            }
        }

        return true
    }

    /** Allows you to set a cooldown for this command. */
    public open fun <T : CooldownType> cooldowns(
        kClass: KClass<T>,
        cooldownBody: suspend MessageCreateEvent.() -> Duration?
    ) {
        this.cooldownTypeKClass = kClass
        this.cooldownBody = cooldownBody
    }

    /** Allows you to set a cooldown for this command. */
    public inline fun <reified T : CooldownType> cooldowns(
        noinline cooldownBody: suspend MessageCreateEvent.() -> Duration?
    ): Unit = cooldowns(T::class, cooldownBody)

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
     * @param args Array of command arguments.
     * @param skipChecks Whether to skip testing the command's checks.
     */
    public open suspend fun call(
        event: MessageCreateEvent,
        commandName: String,
        parser: StringParser,
        argString: String,
        skipChecks: Boolean = false
    ) {
        if (!skipChecks && !runChecks(event)) {
            return
        }

        val context = MessageCommandContext(this, event, commandName, parser, argString)

        context.populate()

        val firstBreadcrumb = if (sentry.enabled) {
            val channel = event.message.getChannelOrNull()
            val guild = event.message.getGuildOrNull()

            val data = mutableMapOf(
                "arguments" to argString,
                "message" to event.message.content
            )

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

            sentry.createBreadcrumb(
                category = "command",
                type = "user",
                message = "Command \"$name\" called.",
                data = data
            )
        } else {
            null
        }

        @Suppress("TooGenericExceptionCaught")  // Anything could happen here
        try {
            if (context.guild != null) {
                val perms = (context.channel.asChannel() as GuildChannel)
                    .getEffectivePermissions(kord.selfId)

                val missingPerms = requiredPerms.filter { !perms.contains(it) }

                if (missingPerms.isNotEmpty()) {
                    throw CommandException(
                        context.translate(
                            "commands.error.missingBotPermissions",
                            null,
                            replacements = arrayOf(
                                missingPerms.map { it.translate(context) }.joinToString(", ")
                            )
                        )
                    )
                }
            }

            if (::cooldownTypeKClass.isInitialized) {
                val cooldownType = settings.slashCommandsBuilder.cooldownsBuilder.registered
                    .find { type -> cooldownTypeKClass.java.isAssignableFrom(type::class.java) }

                if (cooldownType != null) {
                    val key = cooldownType.getCooldownKey(event)
                    if (key != null) {
                        val timeLeft = cooldown.getCooldown(key)

                        if (timeLeft != null && timeLeft > Duration.ZERO) {
                            throw CommandException("You must wait another $timeLeft before using this command.")
                        } else {
                            val cooldownDuration = cooldownBody.invoke(event)
                            if (cooldownDuration != null) {
                                cooldown.setCooldown(key, cooldownDuration)
                            }
                        }
                    }
                }
            }

            if (this.arguments != null) {
                val parsedArgs = this.parser.parse(this.arguments!!, context)
                context.populateArgs(parsedArgs)
            }

            this.body(context)
        } catch (e: CommandException) {
            event.message.respond(e.toString())
        } catch (t: Throwable) {
            if (sentry.enabled) {
                logger.debug { "Submitting error to sentry." }

                lateinit var sentryId: SentryId
                val channel = event.message.getChannelOrNull()

                val translatedName = when (this) {
                    is MessageSubCommand -> this.getFullTranslatedName(context.getLocale())
                    is GroupCommand -> this.getFullTranslatedName(context.getLocale())

                    else -> this.getTranslatedName(context.getLocale())
                }

                Sentry.withScope {
                    val author = event.message.author

                    if (author != null) {
                        it.user(author)
                    }

                    it.tag("private", "false")

                    if (channel is DmChannel) {
                        it.tag("private", "true")
                    }

                    it.tag("command", translatedName)
                    it.tag("extension", extension.name)

                    it.addBreadcrumb(firstBreadcrumb!!)

                    context.breadcrumbs.forEach { breadcrumb -> it.addBreadcrumb(breadcrumb) }

                    sentryId = Sentry.captureException(t, "MessageCommand execution failed.")

                    logger.debug { "Error submitted to Sentry: $sentryId" }
                }

                sentry.addEventId(sentryId)

                logger.error(t) { "Error during execution of $name command ($event)" }

                if (extension.bot.extensions.containsKey("sentry")) {
                    val prefix = messageCommandsRegistry.getPrefix(event)

                    event.message.respond(
                        context.translate(
                            "commands.error.user.sentry.message",
                            null,
                            replacements = arrayOf(
                                prefix,
                                sentryId
                            )
                        )
                    )
                } else {
                    event.message.respond(
                        context.translate("commands.error.user", null)
                    )
                }
            } else {
                logger.error(t) { "Error during execution of $name command ($event)" }

                event.message.respond(
                    context.translate("commands.error.user", null)
                )
            }
        }
    }
}
