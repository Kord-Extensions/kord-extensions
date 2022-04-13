/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.sentry.BreadcrumbType
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.utils.getLocale
import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.interaction.GroupCommand
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import mu.KLogger
import mu.KotlinLogging
import java.util.*

/**
 * Slash command, executed directly in the chat input.
 *
 * @param arguments Callable returning an `Arguments` object, if any
 * @param parentCommand Parent slash command, if any
 * @param parentGroup Parent slash command group, if any
 */
public abstract class SlashCommand<C : SlashCommandContext<*, A>, A : Arguments>(
    extension: Extension,

    public open val arguments: (() -> A)? = null,
    public open val parentCommand: SlashCommand<*, *>? = null,
    public open val parentGroup: SlashGroup? = null
) : ApplicationCommand<ChatInputCommandInteractionCreateEvent>(extension) {
    /** @suppress This is only meant for use by code that extends the command system. **/
    public val kxLogger: KLogger = KotlinLogging.logger {}

    /** Command description, as displayed on Discord. **/
    public open lateinit var description: String

    /** Command body, to be called when the command is executed. **/
    public lateinit var body: suspend C.() -> Unit

    /** Whether this command has a body/action set. **/
    public open val hasBody: Boolean get() = ::body.isInitialized

    /** Map of group names to slash command groups, if any. **/
    public open val groups: MutableMap<String, SlashGroup> = mutableMapOf()

    /** List of subcommands, if any. **/
    public open val subCommands: MutableList<SlashCommand<*, *>> = mutableListOf()

    override val type: ApplicationCommandType = ApplicationCommandType.ChatInput

    /** Translation cache, so we don't have to look up translations every time. **/
    public open val descriptionTranslationCache: MutableMap<Locale, String> = mutableMapOf()

    override var guildId: Snowflake? = if (parentCommand == null && parentGroup == null) {
        settings.applicationCommandsBuilder.defaultGuild
    } else {
        null
    }

    override fun validate() {
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

        if (parentCommand != null && guildId != null) {
            throw InvalidCommandException(
                name,

                "Subcommands may not be limited to specific guilds - set the `guild` property on the parent command " +
                    "instead."
            )
        }
    }

    public override fun getTranslatedName(locale: Locale): String {
        // Only slash commands need this to be lower-cased.

        if (!nameTranslationCache.containsKey(locale)) {
            nameTranslationCache[locale] = translationsProvider.translate(
                this.name,
                this.resolvedBundle,
                locale
            ).lowercase()
        }

        return nameTranslationCache[locale]!!
    }

    /** Return this command's description translated for the given locale, cached as required. **/
    public fun getTranslatedDescription(locale: Locale): String {
        // Only slash commands need this to be lower-cased.

        if (!descriptionTranslationCache.containsKey(locale)) {
            descriptionTranslationCache[locale] = translationsProvider.translate(
                this.description,
                this.resolvedBundle,
                locale
            )
        }

        return descriptionTranslationCache[locale]!!
    }

    /** Call this to supply a command [body], to be called when the command is executed. **/
    public fun action(action: suspend C.() -> Unit) {
        body = action
    }

    /** Override this to implement your command's calling logic. Check subtypes for examples! **/
    public abstract override suspend fun call(event: ChatInputCommandInteractionCreateEvent)

    /** Override this to implement a way to respond to the user, regardless of whatever happens. **/
    public abstract suspend fun respondText(context: C, message: String, failureType: FailureReason<*>)

    /**
     * Override this to implement the final calling logic, including creating the command context and running with it.
     */
    public abstract suspend fun run(event: ChatInputCommandInteractionCreateEvent)

    /** If enabled, adds the initial Sentry breadcrumb to the given context. **/
    public open suspend fun firstSentryBreadcrumb(context: C, commandObj: SlashCommand<*, *>) {
        if (sentry.enabled) {
            context.sentry.breadcrumb(BreadcrumbType.User) {
                category = "command.application.slash"
                message = "Slash command \"${commandObj.name}\" called."

                val channel = context.channel.asChannelOrNull()
                val guild = context.guild?.asGuildOrNull()

                data["command"] = commandObj.name

                if (guildId != null) {
                    data["command.guild"] = guildId.toString()
                }

                if (channel != null) {
                    data["channel"] = when (channel) {
                        is DmChannel -> "Private Message (${channel.id})"
                        is GuildMessageChannel -> "#${channel.name} (${channel.id})"

                        else -> channel.id.toString()
                    }
                }

                if (guild != null) {
                    data["guild"] = "${guild.name} (${guild.id})"
                }
            }
        }
    }

    override suspend fun runChecks(event: ChatInputCommandInteractionCreateEvent): Boolean {
        val locale = event.getLocale()
        var result = super.runChecks(event)

        if (result && parentCommand != null) {
            result = parentCommand!!.runChecks(event)
        }

        if (result && parentGroup != null) {
            result = parentGroup!!.parent.runChecks(event)
        }

        if (result) {
            settings.applicationCommandsBuilder.slashCommandChecks.forEach { check ->
                val context = CheckContext(event, locale)

                check(context)

                if (!context.passed) {
                    context.throwIfFailedWithMessage()

                    return false
                }
            }

            extension.slashCommandChecks.forEach { check ->
                val context = CheckContext(event, locale)

                check(context)

                if (!context.passed) {
                    context.throwIfFailedWithMessage()

                    return false
                }
            }
        }

        return result
    }

    /** Given a command event, resolve the correct command or subcommand object. **/
    public open fun findCommand(event: ChatInputCommandInteractionCreateEvent): SlashCommand<*, *> =
        findCommand(event.interaction.command)

    /** Given an autocomplete event, resolve the correct command or subcommand object. **/
    public open fun findCommand(event: AutoCompleteInteractionCreateEvent): SlashCommand<*, *> =
        findCommand(event.interaction.command)

    /** Given an [InteractionCommand], resolve the correct command or subcommand object. **/
    public open fun findCommand(eventCommand: InteractionCommand): SlashCommand<*, *> =
        when (eventCommand) {
            is SubCommand -> {
                val firstSubCommandKey = eventCommand.name

                this.subCommands.firstOrNull { it.name == firstSubCommandKey }
                    ?: error("Unknown subcommand: $firstSubCommandKey")
            }

            is GroupCommand -> {
                val firstEventGroupKey = eventCommand.groupName
                val group = this.groups[firstEventGroupKey] ?: error("Unknown command group: $firstEventGroupKey")
                val firstSubCommandKey = eventCommand.name

                group.subCommands.firstOrNull { it.name == firstSubCommandKey }
                    ?: error("Unknown subcommand: $firstSubCommandKey")
            }

            else -> this
        }

    /** A general way to handle errors thrown during the course of a command's execution. **/
    public open suspend fun handleError(context: C, t: Throwable, commandObj: SlashCommand<*, *>) {
        kxLogger.error(t) { "Error during execution of ${commandObj.name} slash command (${context.event})" }

        if (sentry.enabled) {
            kxLogger.trace { "Submitting error to sentry." }

            val channel = context.channel
            val author = context.user.asUserOrNull()

            val sentryId = context.sentry.captureException(t, "Slash command execution failed.") {
                if (author != null) {
                    user(author)
                }

                tag("private", "false")

                if (channel is DmChannel) {
                    tag("private", "true")
                }

                tag("command", commandObj.name)
                tag("extension", commandObj.extension.name)
            }

            kxLogger.info { "Error submitted to Sentry: $sentryId" }

            val errorMessage = if (extension.bot.extensions.containsKey("sentry")) {
                context.translate("commands.error.user.sentry.slash", null, replacements = arrayOf(sentryId))
            } else {
                context.translate("commands.error.user", null)
            }

            respondText(context, errorMessage, FailureReason.ExecutionError(t))
        } else {
            respondText(
                context,
                context.translate("commands.error.user", null),
                FailureReason.ExecutionError(t)
            )
        }
    }
}
