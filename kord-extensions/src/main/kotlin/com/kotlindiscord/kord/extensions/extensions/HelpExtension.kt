package com.kotlindiscord.kord.extensions.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.GroupCommand
import com.kotlindiscord.kord.extensions.commands.MessageCommand
import com.kotlindiscord.kord.extensions.commands.MessageSubCommand
import com.kotlindiscord.kord.extensions.pagination.Paginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Number of help per page, when it is invoked without any parameter. */
public const val HELP_PER_PAGE: Int = 4

private const val PAGE_TIMEOUT = 60_000L  // 60 seconds

/**
 * Help command extension.
 *
 * This extension provides a `!help` command listing the available commands,
 * along with a `!help <command>` to get more info about a specific command.
 */
public class HelpExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "help"

    override suspend fun setup() {
        command {
            name = "help"
            aliases = arrayOf("h")
            description = "Get command help.\n" +
                "\n" +
                "Specify the name of a command to get help for that specific command."
            signature = "[command]"

            action {
                if (args.isEmpty()) {
                    Paginator(
                        bot,
                        targetMessage = message,
                        pages = formatMainHelp(gatherCommands(event), event),
                        owner = message.author,
                        timeout = PAGE_TIMEOUT,
                        keepEmbed = true
                    ).send()
                } else {
                    message.channel.createEmbed {
                        val command = getCommand(args)

                        title = "MessageCommand Help"
                        description = if (command == null) {
                            "Unknown command."
                        } else {
                            formatLongHelp(command)
                        }
                    }
                }
            }
        }
    }

    /**
     * Gather all available commands from the bot, and return them as an array of [MessageCommand].
     */
    public suspend fun gatherCommands(event: MessageCreateEvent): List<MessageCommand> =
        bot.commands.filter { !it.hidden && it.enabled && it.runChecks(event) }

    /**
     * Generate help message by formatting a [List] of [MessageCommand] objects.
     */
    public suspend fun formatMainHelp(commands: List<MessageCommand>, event: MessageCreateEvent): Pages {
        val pages = Pages()
        var totalCommands = 0

        commands.filter { it.runChecks(event) }.sortedBy { it.name }.chunked(HELP_PER_PAGE).map { list ->
            list.joinToString(separator = "\n\n") { command ->
                totalCommands += 1

                with(command) {
                    var desc = "**${bot.prefix}$name $signature**\n${description.takeWhile { it != '\n' }}\n"

                    if (aliases.isNotEmpty()) {
                        desc += "\n**Aliases: **" + aliases.joinToString(", ") { "`$it`" }
                    }

                    if (this is GroupCommand) {
                        desc += "\n**Subcommands:** " + this.commands.joinToString(", ") { "`${it.name}`" }
                    }

                    desc
                }
            }
        }.forEach {
            pages.addPage(
                Page(
                    description = it,
                    title = "All Commands",
                    footer = "$totalCommands commands available"
                )
            )
        }

        return pages
    }

    /**
     * Return the [MessageCommand] specified in the arguments, or null if it can't be found.
     */
    public fun getCommand(args: Array<String>): MessageCommand? {
        val firstArg = args.first()

        var command: MessageCommand? = bot.commands.firstOrNull { it.name == firstArg || it.aliases.contains(firstArg) }

        args.drop(1).forEach {
            if (command != null && command is GroupCommand) {
                command = (command as GroupCommand).getCommand(it)
            }
        }

        return command
    }

    /**
     * Format the given command's description into a long help string.
     *
     * @param command The command to format the description of.
     */
    public fun formatLongHelp(command: MessageCommand): String {
        val name = when (command) {
            is MessageSubCommand -> command.getFullName()
            is GroupCommand -> command.getFullName()

            else -> command.name
        }

        var desc = "**${bot.prefix}$name ${command.signature}**\n\n${command.description}\n"

        if (command.aliases.isNotEmpty()) {
            desc += "\n**Aliases: **" + command.aliases.joinToString(", ") { "`$it`" }
        }

        if (command is GroupCommand) {
            desc += "\n**Subcommands: ** " + command.commands.joinToString(", ") { "`${it.name}`" }
        }

        return desc
    }
}
