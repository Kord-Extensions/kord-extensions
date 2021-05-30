@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.commands.slash

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.parser.SlashCommandParser
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.KordObject
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.interaction.GroupCommand
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.InteractionCreateEvent
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

private val logger: KLogger = KotlinLogging.logger {}
private const val DISCORD_LIMIT: Int = 25

/**
 * Class representing a slash command.
 *
 * You shouldn't need to use this class directly - instead, create an [Extension] and use the
 * [slash command function][Extension.slashCommand] to register your command, by overriding the [Extension.setup]
 * function.
 *
 * @param extension The [Extension] that registered this command.
 * @param arguments Arguments object builder for this command, if it has arguments.
 * @param parentCommand If this is a subcommand, the root command this command belongs to.
 * @param parentGroup If this is a grouped subcommand, the group this command belongs to.
 */
@ExtensionDSL
public open class SlashCommand<T : Arguments>(
    extension: Extension,
    public open val arguments: (() -> T)? = null,

    public open val parentCommand: SlashCommand<out Arguments>? = null,
    public open val parentGroup: SlashGroup? = null
) : Command(extension), KoinComponent {
    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    private val settings: ExtensibleBotBuilder by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public val kord: Kord by inject()

    /** Sentry adapter, for easy access to Sentry functions. **/
    public val sentry: SentryAdapter by inject()

    /** Command description, as displayed on Discord. **/
    public open lateinit var description: String

    /** @suppress **/
    public open lateinit var body: suspend SlashCommandContext<out T>.() -> Unit

    /** Whether this command has a body/action set. **/
    public open val hasBody: Boolean get() = ::body.isInitialized

    /** Guild ID this slash command is to be registered for, if any. **/
    public open var guild: Snowflake? = settings.slashCommandsBuilder.defaultGuild

    /** Type of automatic ack to use, if any. **/
    public open var autoAck: AutoAckType = AutoAckType.EPHEMERAL

    /** Map of group names to slash command groups, if any. **/
    public open val groups: MutableMap<String, SlashGroup> = mutableMapOf()

    /** List of subcommands, if any. **/
    public open val subCommands: MutableList<SlashCommand<out Arguments>> = mutableListOf()

    /** @suppress **/
    public open val checkList: MutableList<suspend (InteractionCreateEvent) -> Boolean> = mutableListOf()

    public override val parser: SlashCommandParser = SlashCommandParser()

    /** Permissions required to be able to run this command. **/
    public open val requiredPerms: MutableSet<Permission> = mutableSetOf()

    /** Translation cache, so we don't have to look up translations every time. **/
    public open val nameTranslationCache: MutableMap<Locale, String> = mutableMapOf()

    /** Return this command's name translated for the given locale, cached as required. **/
    public open fun getTranslatedName(locale: Locale): String {
        if (!nameTranslationCache.containsKey(locale)) {
            nameTranslationCache[locale] = translationsProvider.translate(
                this.name,
                this.extension.bundle,
                locale
            ).toLowerCase()
        }

        return nameTranslationCache[locale]!!
    }

    /**
     * An internal function used to ensure that all of a command's required properties are present.
     *
     * @throws InvalidCommandException Thrown when a required property hasn't been set.
     */
    @Throws(InvalidCommandException::class)
    public override fun validate() {
        super.validate()

        if (!::description.isInitialized) {
            throw InvalidCommandException(name, "No command description given.")
        }

        if (!::body.isInitialized && groups.isEmpty() && subCommands.isEmpty()) {
            throw InvalidCommandException(name, "No command action or subcommands/groups given.")
        }

        if (::body.isInitialized && !(groups.isEmpty() && subCommands.isEmpty())) {
            throw InvalidCommandException(
                name,

                "Command action and subcommands/groups given, but slash commands may not have an action if they have" +
                    " a subcommand or group."
            )
        }

        if (parentCommand != null && guild != null) {
            throw InvalidCommandException(
                name,

                "Subcommands may not be limited to specific guilds - set the `guild` property on the parent command " +
                    "instead."
            )
        }
    }

    /** If your bot requires permissions to be able to execute the command, add them using this function. **/
    public fun requirePermissions(vararg perms: Permission) {
        perms.forEach { requiredPerms.add(it) }
    }

    // region: DSL functions

    /**
     * Create a command group, using the given name.
     *
     * Note that only root/top-level commands can contain command groups. An error will be thrown if you try to use
     * this with a subcommand.
     *
     * @param name Name of the command group on Discord.
     * @param body Lambda used to build the [SlashGroup] object.
     */
    public open suspend fun group(name: String, body: suspend SlashGroup.() -> Unit): SlashGroup {
        if (parentCommand != null) {
            error("Command groups may not be nested inside subcommands.")
        }

        if (subCommands.isNotEmpty()) {
            error("Commands may only contain subcommands or command groups, not both.")
        }

        if (groups.size >= DISCORD_LIMIT) {
            error("Commands may only contain up to $DISCORD_LIMIT command groups.")
        }

        if (groups[name] != null) {
            error("A command group with the name '$name' has already been registered.")
        }

        val group = SlashGroup(name, this)

        body.invoke(group)
        group.validate()

        groups[name] = group

        return group
    }

    /** Specify a specific guild for this slash command. **/
    public open fun guild(guild: Snowflake) {
        this.guild = guild
    }

    /** Specify a specific guild for this slash command. **/
    public open fun guild(guild: Long) {
        this.guild = Snowflake(guild)
    }

    /** Specify a specific guild for this slash command. **/
    public open fun guild(guild: GuildBehavior) {
        this.guild = guild.id
    }

    /**
     * DSL function for easily registering a subcommand, with arguments.
     *
     * Use this in your setup function to register a subcommand that may be executed on Discord.
     *
     * @param arguments Arguments builder (probably a reference to the class constructor).
     * @param body Builder lambda used for setting up the slash command object.
     */
    public open suspend fun <T : Arguments> subCommand(
        arguments: (() -> T),
        body: suspend SlashCommand<T>.() -> Unit
    ): SlashCommand<T> {
        val commandObj = SlashCommand(this.extension, arguments, this)
        body.invoke(commandObj)

        return subCommand(commandObj)
    }

    /**
     * DSL function for easily registering a subcommand, without arguments.
     *
     * Use this in your slash command function to register a subcommand that may be executed on Discord.
     *
     * @param body Builder lambda used for setting up the subcommand object.
     */
    public open suspend fun subCommand(
        body: suspend SlashCommand<out Arguments>.() -> Unit
    ): SlashCommand<out Arguments> {
        val commandObj = SlashCommand<Arguments>(this.extension, null, this)
        body.invoke(commandObj)

        return subCommand(commandObj)
    }

    /**
     * Function for registering a custom slash command object, for subcommands.
     *
     * You can use this if you have a custom slash command subclass you need to register.
     *
     * @param commandObj SlashCommand object to register as a subcommand.
     */
    public open suspend fun <T : Arguments> subCommand(
        commandObj: SlashCommand<T>
    ): SlashCommand<T> {
        if (parentCommand != null) {
            error("Subcommands may not be nested inside subcommands.")
        }

        if (groups.isNotEmpty()) {
            error("Commands may only contain subcommands or command groups, not both.")
        }

        if (subCommands.size >= DISCORD_LIMIT) {
            error("Commands may only contain up to $DISCORD_LIMIT top-level subcommands.")
        }

        try {
            commandObj.validate()
            subCommands.add(commandObj)
        } catch (e: CommandRegistrationException) {
            logger.error(e) { "Failed to register subcommand - $e" }
        } catch (e: InvalidCommandException) {
            logger.error(e) { "Failed to register subcommand - $e" }
        }

        return commandObj
    }

    /**
     * Define what will happen when your command is invoked.
     *
     * @param action The body of your command, which will be executed when your command is invoked.
     */
    public open fun action(action: suspend SlashCommandContext<out T>.() -> Unit) {
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
    public open fun check(vararg checks: suspend (InteractionCreateEvent) -> Boolean) {
        checks.forEach { checkList.add(it) }
    }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to this command.
     */
    public open fun check(check: suspend (InteractionCreateEvent) -> Boolean) {
        checkList.add(check)
    }

    // endregion

    /** Run checks with the provided [InteractionCreateEvent]. Return false if any failed, true otherwise. **/
    public open suspend fun runChecks(event: InteractionCreateEvent): Boolean {
        // global checks
        for (check in extension.bot.settings.slashCommandsBuilder.checkList) {
            if (!check.invoke(event)) {
                return false
            }
        }

        // local extension checks
        for (check in extension.slashCommandChecks) {
            if (!check.invoke(event)) {
                return false
            }
        }

        if (parentCommand != null) {
            val parentChecks = parentCommand!!.runChecks(event)

            if (!parentChecks) {
                return false
            }
        }

        for (check in checkList) {
            if (!check.invoke(event)) {
                return false
            }
        }
        return true
    }

    /**
     * Execute this command, given an [InteractionCreateEvent].
     *
     * This function takes a [InteractionCreateEvent] (generated when a slash command is executed), and
     * processes it. The command's checks are invoked and, assuming all of the
     * checks passed, the [command body][action] is executed.
     *
     * If an exception is thrown by the [command body][action], it is caught and a traceback
     * is printed.
     *
     * @param event The interaction creation event.
     */
    public open suspend fun call(event: InteractionCreateEvent) {
        val eventCommand = event.interaction.command

        // We lie to the compiler thrice below to work around an issue with generics.
        val commandObj: SlashCommand<Arguments> = if (eventCommand is SubCommand) {
            val firstSubCommandKey = eventCommand.name

            this.subCommands.firstOrNull { it.name == firstSubCommandKey } as SlashCommand<Arguments>?
                ?: error("Unknown subcommand: $firstSubCommandKey")
        } else if (eventCommand is GroupCommand) {
            val firstEventGroupKey = eventCommand.groupName
            val group = this.groups[firstEventGroupKey] ?: error("Unknown command group: $firstEventGroupKey")
            val firstSubCommandKey = eventCommand.name

            group.subCommands.firstOrNull { it.name == firstSubCommandKey } as SlashCommand<Arguments>?
                ?: error("Unknown subcommand: $firstSubCommandKey")
        } else {
            this as SlashCommand<Arguments>
        }

        if (!commandObj.runChecks(event)) {
            return
        }

        val resp = when (commandObj.autoAck) {
            AutoAckType.EPHEMERAL -> event.interaction.acknowledgeEphemeral()
            AutoAckType.PUBLIC -> event.interaction.ackowledgePublic()

            AutoAckType.NONE -> null
        }

        val context = SlashCommandContext(commandObj, event, commandObj.name, resp)

        context.populate()

        val firstBreadcrumb = if (sentry.enabled) {
            val channel = context.channel.asChannelOrNull()
            val guild = context.guild?.asGuildOrNull()

            val data = mutableMapOf(
                "command" to commandObj.name
            )

            if (this.guild != null) {
                data["command.guild"] to this.guild!!.asString
            }

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
                category = "command.slash",
                type = "user",
                message = "Slash command \"${commandObj.name}\" called.",
                data = data
            )
        } else {
            null
        }

        @Suppress("TooGenericExceptionCaught")
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

            if (commandObj.arguments != null) {
                val args = commandObj.parser.parse(commandObj.arguments!!, context)
                context.populateArgs(args)
            }

            commandObj.body(context)
        } catch (e: CommandException) {
            respondText(context, e.reason)
        } catch (t: Throwable) {
            if (sentry.enabled) {
                logger.debug { "Submitting error to sentry." }

                lateinit var sentryId: SentryId

                val channel = context.channel
                val author = context.user.asUserOrNull()

                Sentry.withScope {
                    if (author != null) {
                        it.user(author)
                    }

                    it.tag("private", "false")

                    if (channel is DmChannel) {
                        it.tag("private", "true")
                    }

                    it.tag("command", commandObj.name)
                    it.tag("extension", commandObj.extension.name)

                    it.addBreadcrumb(firstBreadcrumb!!)

                    context.breadcrumbs.forEach { breadcrumb -> it.addBreadcrumb(breadcrumb) }

                    sentryId = Sentry.captureException(t, "SlashCommand execution failed.")

                    logger.debug { "Error submitted to Sentry: $sentryId" }
                }

                sentry.addEventId(sentryId)

                logger.error(t) { "Error during execution of ${commandObj.name} slash command ($event)" }

                val errorMessage = if (extension.bot.extensions.containsKey("sentry")) {
                    context.translate("commands.error.user.sentry.slash", null, replacements = arrayOf(sentryId))
                } else {
                    context.translate("commands.error.user", null)
                }

                respondText(context, errorMessage)
            } else {
                logger.error(t) { "Error during execution of ${commandObj.name} slash command ($event)" }

                respondText(context, context.translate("commands.error.user", null))
            }
        }
    }

    private suspend fun respondText(
        context: SlashCommandContext<*>,
        text: String
    ): KordObject = when (context.isEphemeral) {
        null -> {
            context.ack(true)
            context.ephemeralFollowUp(text)
        }

        true -> context.ephemeralFollowUp(text)
        false -> context.publicFollowUp { content = text }
    }
}
