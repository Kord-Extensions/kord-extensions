@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.extensions.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.GroupCommand
import com.kotlindiscord.kord.extensions.commands.MessageCommand
import com.kotlindiscord.kord.extensions.commands.MessageCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.stringList
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.base.HelpProvider
import com.kotlindiscord.kord.extensions.pagination.Paginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.utils.getLocale
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.message.MessageCreateEvent
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Number of commands to show per page. */
public const val HELP_PER_PAGE: Int = 4

private const val PAGE_TIMEOUT = 60_000L  // 60 seconds
private const val COMMANDS_GROUP = ""
private const val ARGUMENTS_GROUP = "Arguments"

/**
 * Help command extension.
 *
 * This extension provides a `!help` command listing the available commands,
 * along with a `!help <command>` to get more info about a specific command.
 */
@Suppress("StringLiteralDuplication")
public class HelpExtension(bot: ExtensibleBot) : HelpProvider, Extension(bot) {
    override val name: String = "help"

    override suspend fun setup() {
        command(::HelpArguments) {
            name = "extensions.help.commandName"
            aliases = arrayOf("extensions.help.commandAlias.h")
            description = "extensions.help.commandDescription"

            action {
                if (arguments.command.isEmpty()) {
                    getMainHelpPaginator(this).send()
                } else {
                    getCommandHelpPaginator(this, arguments.command).send()
                }
            }
        }
    }

    override suspend fun getMainHelpPaginator(event: MessageCreateEvent, prefix: String): Paginator {
        var totalCommands = 0
        val locale = event.getLocale(bot)

        val pages = Pages(COMMANDS_GROUP)
        val commandPages = gatherCommands(event)
            .chunked(HELP_PER_PAGE)
            .map { list ->
                list.map {
                    totalCommands += 1

                    formatCommandHelp(prefix, event, it)
                }
            }

        for (page in commandPages) {
            pages.addPage(
                COMMANDS_GROUP,

                Page(
                    description = page.joinToString("\n\n") { "${it.first}\n${it.second}" },
                    title = bot.translationsProvider.translate("extensions.help.paginator.title.commands", locale),
                    footer = bot.translationsProvider.translate(
                        "extensions.help.paginator.footer",
                        locale,
                        replacements = arrayOf(totalCommands)
                    )
                )
            )

            pages.addPage(
                ARGUMENTS_GROUP,

                Page(
                    description = page.joinToString("\n\n") { "${it.first}\n${it.third}" },
                    title = bot.translationsProvider.translate("extensions.help.paginator.title.arguments", locale),
                    footer = bot.translationsProvider.translate(
                        "extensions.help.paginator.footer",
                        locale,
                        replacements = arrayOf(totalCommands)
                    )
                )
            )
        }

        if (totalCommands < 1) {
            // This should never happen in most cases, but it's best to be safe about it

            pages.addPage(
                COMMANDS_GROUP,
                Page(
                    description = bot.translationsProvider.translate("extensions.help.paginator.noCommands", locale),
                    title = bot.translationsProvider.translate("extensions.help.paginator.noCommands", locale),
                    footer = bot.translationsProvider.translate(
                        "extensions.help.paginator.footer",
                        locale,
                        replacements = arrayOf(0)
                    )
                )
            )
        }

        return Paginator(
            bot,
            targetMessage = event.message,
            pages = pages,
            owner = event.message.author,
            timeout = PAGE_TIMEOUT,
            keepEmbed = true,
            locale = locale
        )
    }

    override suspend fun getCommandHelpPaginator(
        event: MessageCreateEvent,
        prefix: String,
        args: List<String>
    ): Paginator = getCommandHelpPaginator(event, prefix, getCommand(event, args))

    override suspend fun getCommandHelpPaginator(context: MessageCommandContext<*>, args: List<String>): Paginator =
        getCommandHelpPaginator(context, getCommand(context.event, args))

    override suspend fun getCommandHelpPaginator(
        event: MessageCreateEvent,
        prefix: String,
        command: MessageCommand<out Arguments>?
    ): Paginator {
        val pages = Pages(COMMANDS_GROUP)
        val locale = event.getLocale(bot)

        if (command == null || !command.runChecks(event)) {
            pages.addPage(
                COMMANDS_GROUP,

                Page(
                    description = bot.translationsProvider.translate(
                        "extensions.help.error.missingCommandDescription",
                        locale
                    ),

                    title = bot.translationsProvider.translate(
                        "extensions.help.error.missingCommandTitle",
                        locale
                    )
                )
            )
        } else {
            val (openingLine, desc, arguments) = formatCommandHelp(prefix, event, command, longDescription = true)

            pages.addPage(
                COMMANDS_GROUP,

                Page(
                    description = "$openingLine\n$desc\n\n$arguments",

                    title = bot.translationsProvider.translate(
                        "extensions.help.paginator.title.command",
                        locale,
                        replacements = arrayOf(command.getTranslatedName(locale))
                    )
                )
            )
        }

        return Paginator(
            bot,
            targetMessage = event.message,
            pages = pages,
            owner = event.message.author,
            timeout = PAGE_TIMEOUT,
            keepEmbed = true,
            locale = locale
        )
    }

    override suspend fun gatherCommands(event: MessageCreateEvent): List<MessageCommand<out Arguments>> =
        bot.messageCommands.commands.filter { !it.hidden && it.enabled && it.runChecks(event) }.sortedBy { it.name }

    override suspend fun formatCommandHelp(
        prefix: String,
        event: MessageCreateEvent,
        command: MessageCommand<out Arguments>,
        longDescription: Boolean
    ): Triple<String, String, String> {
        val locale = event.getLocale(bot)
        val openingLine = "**$prefix${command.getTranslatedName(locale)} ${command.getSignature(locale)}**\n"

        var description = if (longDescription) {
            bot.translationsProvider.translate(command.description, command.extension.bundle, locale)
        } else {
            bot.translationsProvider.translate(command.description, command.extension.bundle, locale)
                .takeWhile { it != '\n' }
        } + "\n"

        if (command.aliases.isNotEmpty()) {
            description += "\n"

            description += bot.translationsProvider.translate(
                "extensions.help.commandDescription.aliases",
                locale
            )

            description += " "
            description += command.getTranslatedAliases(locale).joinToString(", ") {
                "`$it`"
            }
        }

        if (command is GroupCommand) {
            val subCommands = command.commands.filter { it.runChecks(event) }

            if (subCommands.isNotEmpty()) {
                description += "\n"

                description += bot.translationsProvider.translate(
                    "extensions.help.commandDescription.subCommands",
                    locale
                )

                description += " "
                description += subCommands.map { it.getTranslatedName(locale) }.joinToString(", ") {
                    "`$it`"
                }
            }
        }

        if (command.requiredPerms.isNotEmpty()) {
            description += "\n"

            description += bot.translationsProvider.translate(
                "extensions.help.commandDescription.requiredBotPermissions",
                locale
            )

            description += " "
            description += command.requiredPerms.map { it.translate(locale, bot) }.joinToString(", ")
        }

        var arguments = "\n\n"

        if (command.arguments == null) {
            arguments += bot.translationsProvider.translate(
                "extensions.help.commandDescription.noArguments",
                locale
            )
        } else {
            @Suppress("TooGenericExceptionCaught")  // Hard to say really
            arguments += try {
                val argsObj = command.arguments!!.invoke()

                argsObj.args.joinToString("\n") {
                    var desc = "**»** `${it.displayName}"

                    if (it.converter.showTypeInSignature) {
                        desc += " ("

                        desc += bot.translationsProvider.translate(
                            it.converter.signatureTypeString,
                            it.converter.bundle,
                            locale
                        )

                        desc += ")"
                    }

                    desc += "`: "
                    desc += bot.translationsProvider.translate(it.description, command.extension.bundle, locale)

                    desc
                }
            } catch (t: Throwable) {
                logger.error(t) { "Failed to retrieve argument list for command: $name" }

                bot.translationsProvider.translate("extensions.help.commandDescription.error.argumentList", locale)
            }
        }

        return Triple(openingLine.trim('\n'), description.trim('\n'), arguments.trim('\n'))
    }

    override suspend fun getCommand(event: MessageCreateEvent, args: List<String>): MessageCommand<out Arguments>? {
        val locale = event.getLocale(bot)
        val firstArg = args.first()

        var command: MessageCommand<out Arguments>? = bot.messageCommands.commands.firstOrNull {
            (it.getTranslatedName(locale) == firstArg || it.getTranslatedAliases(locale).contains(firstArg)) &&
                it.runChecks(event)
        }

        args.drop(1).forEach {
            if (command is GroupCommand<out Arguments>) {
                val gc = command as GroupCommand<out Arguments>

                command = if (gc.runChecks(event)) {
                    gc.getCommand(it, event)
                } else {
                    null
                }
            }
        }

        return command
    }

    /** Help command arguments class. **/
    public class HelpArguments : Arguments() {
        /** Command to get help for. **/
        public val command: List<String> by stringList(
            "command",
            "extensions.help.commandArguments.command",
            false
        )
    }
}
