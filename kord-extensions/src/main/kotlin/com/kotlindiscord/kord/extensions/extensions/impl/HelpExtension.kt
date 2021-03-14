package com.kotlindiscord.kord.extensions.extensions.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.GroupCommand
import com.kotlindiscord.kord.extensions.commands.MessageCommand
import com.kotlindiscord.kord.extensions.commands.converters.stringList
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.base.HelpProvider
import com.kotlindiscord.kord.extensions.pagination.Paginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
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
public class HelpExtension(bot: ExtensibleBot) : HelpProvider, Extension(bot) {
    override val name: String = "help"

    override suspend fun setup() {
        command(::HelpArguments) {
            name = "help"
            aliases = arrayOf("h")
            description = "Get command help.\n\n" +

                "Specify the name of a command to get help for that specific command. Subcommands may also be " +
                "specified, using the same form you'd use to run them."

            action {
                val prefix = bot.messageCommands.getPrefix(event)

                if (arguments.command.isEmpty()) {
                    getMainHelpPaginator(event, prefix).send()
                } else {
                    getCommandHelpPaginator(event, prefix, arguments.command).send()
                }
            }
        }
    }

    override suspend fun getMainHelpPaginator(event: MessageCreateEvent, prefix: String): Paginator {
        var totalCommands = 0

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
                    description = page.joinToString("\n\n") { it.first + it.second },
                    title = "Commands",
                    footer = "$totalCommands commands available"
                )
            )

            pages.addPage(
                ARGUMENTS_GROUP,

                Page(
                    description = page.joinToString("\n\n") { it.first + it.third },
                    title = "Command Arguments",
                    footer = "$totalCommands commands available"
                )
            )
        }

        if (totalCommands < 1) {
            // This should never happen in most cases, but it's best to be safe about it

            pages.addPage(
                COMMANDS_GROUP,
                Page(
                    description = "No commands found.",
                    title = "No commands found",
                    footer = "0 commands available"
                )
            )
        }

        return Paginator(
            bot,
            targetMessage = event.message,
            pages = pages,
            owner = event.message.author,
            timeout = PAGE_TIMEOUT,
            keepEmbed = true
        )
    }

    override suspend fun getCommandHelpPaginator(
        event: MessageCreateEvent,
        prefix: String,
        args: List<String>
    ): Paginator = getCommandHelpPaginator(event, prefix, getCommand(event, args))

    override suspend fun getCommandHelpPaginator(
        event: MessageCreateEvent,
        prefix: String,
        command: MessageCommand<out Arguments>?
    ): Paginator {
        val pages = Pages(COMMANDS_GROUP)

        if (command == null || !command.runChecks(event)) {
            pages.addPage(
                COMMANDS_GROUP,

                Page(
                    description = "Unable to find that command. This may be for one of several possible reasons:\n\n" +

                        "**»** The command doesn't exist or failed to load\n" +
                        "**»** The command isn't available in this context\n" +
                        "**»** You don't have access to the command\n\n" +

                        "If you feel that this is incorrect, please contact a member of staff.",
                    title = "Command not found"
                )
            )
        } else {
            val (openingLine, desc, arguments) = formatCommandHelp(prefix, event, command, longDescription = true)

            pages.addPage(
                COMMANDS_GROUP,

                Page(
                    description = openingLine + desc + arguments,
                    title = "Command: ${command.name}"
                )
            )
        }

        return Paginator(
            bot,
            targetMessage = event.message,
            pages = pages,
            owner = event.message.author,
            timeout = PAGE_TIMEOUT,
            keepEmbed = true
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
        val openingLine = "**$prefix${command.name} ${command.signature}**\n"

        var description = if (longDescription) {
            command.description
        } else {
            command.description.takeWhile { it != '\n' }
        } + "\n"

        if (command.aliases.isNotEmpty()) {
            description += "\n**Aliases:** " + command.aliases.joinToString(", ") { "`$it`" }
        }

        if (command is GroupCommand) {
            val subCommands = command.commands.filter { it.runChecks(event) }

            if (subCommands.isNotEmpty()) {
                description += "\n**Subcommands:** " + subCommands.joinToString(", ") { "`${it.name}`" }
            }
        }

        var arguments = "\n"

        if (command.arguments == null) {
            arguments += "No arguments."
        } else {
            @Suppress("TooGenericExceptionCaught")  // Hard to say really
            arguments += try {
                val argsObj = command.arguments!!.invoke()

                argsObj.args.joinToString("\n") {
                    var desc = "**»** `${it.displayName}"

                    if (it.converter.showTypeInSignature) {
                        desc += " (${it.converter.signatureTypeString})"
                    }

                    desc += "`: ${it.description}"

                    desc
                }
            } catch (t: Throwable) {
                logger.error(t) { "Failed to retrieve argument list for command: $name" }

                "Failed to retrieve argument list due to an error."
            }
        }

        return Triple(openingLine, description, arguments)
    }

    override suspend fun getCommand(event: MessageCreateEvent, args: List<String>): MessageCommand<out Arguments>? {
        val firstArg = args.first()

        var command: MessageCommand<out Arguments>? = bot.messageCommands.commands.firstOrNull {
            (it.name == firstArg || it.aliases.contains(firstArg)) && it.runChecks(event)
        }

        args.drop(1).forEach {
            if (command is GroupCommand<out Arguments>) {
                val gc = command as GroupCommand<out Arguments>

                command = if (gc.runChecks(event)) {
                    gc.getCommand(it)
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
            "Command to get help for",
            false
        )
    }
}
