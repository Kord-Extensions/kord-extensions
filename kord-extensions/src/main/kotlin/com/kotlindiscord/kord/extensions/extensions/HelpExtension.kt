package com.kotlindiscord.kord.extensions.extensions

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.Paginator
import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.GroupCommand
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Number of help per page, when it is invoked without any parameter. */
const val HELP_PER_PAGE = 4

/**
 * Help command extension.
 *
 * This extension provides a `!help` command listing the available commands,
 * along with a `!help <command>` to get more info about a specific command.
 */
class HelpExtension(bot: ExtensibleBot) : Extension(bot) {
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
                        message.channel,
                        "Command Help",
                        formatMainHelp(gatherCommands(event)),
                        owner = message.author,
                        timeout = 10_000L,
                        keepEmbed = true
                    ).send()
                } else {
                    message.channel.createEmbed {
                        val command = getCommand(args)

                        title = "Command Help"
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
     * Gather all available commands from the bot, and return them as an array of [Command].
     */
    suspend fun gatherCommands(event: MessageCreateEvent) =
        bot.commands.filter { !it.hidden && it.enabled && it.runChecks(event) }

    /**
     * Generate help message by formatting a [List] of [Command] objects.
     */
    fun formatMainHelp(commands: List<Command>): List<String> {
        return commands.sortedBy { it.name }.chunked(HELP_PER_PAGE).map { list ->
            list.joinToString(separator = "\n\n") { command ->
                with(command) {
                    var desc = "**${bot.prefix}$name $signature**\n${description.takeWhile { it != '\n' }}"

                    if (command is GroupCommand) {
                        desc += "\n\n**Subcommands:** " + command.commands.joinToString(", ") { "`$it.name`" }
                    }

                    desc
                }
            }
        }
    }

    /**
     * Return the [Command] specified in the arguments, or null if it can't be found.
     */
    fun getCommand(args: Array<String>): Command? {
        val firstArg = args.first()

        var command: Command? = bot.commands.firstOrNull { it.name == firstArg || it.aliases.contains(firstArg) }

        args.drop(1).forEach {
            if (command != null && command is GroupCommand) {
                command = (command as GroupCommand).getCommand(it)
            }
        }

        return command
    }

    /**
     * Format the given command's description into a short help string.
     *
     * @param command The command to format the description of.
     */
    fun formatLongHelp(command: Command): String {
        var desc = "**${bot.prefix}${command.name} ${command.signature}**\n\n" +
            "*${command.description}*"

        if (command is GroupCommand) {
            desc += "\n\n**Subcommands:** " + command.commands.joinToString(", ") { "`$it.name`" }
        }

        return desc
    }
}
