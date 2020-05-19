package com.kotlindiscord.kord.extensions.extensions

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.Paginator
import com.kotlindiscord.kord.extensions.commands.Command
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
            description = "Get help.\n" +
                    "\n" +
                    "Let's just pretend we have a lot of things to say here"
            signature = "[command]"

            action {
                if (args.isEmpty()) {
                    Paginator(
                        bot,
                        message.channel,
                        "Command Help",
                        formatMainHelp(gatherCommands()),
                        owner = message.author,
                        timeout = 10_000L,
                        keepEmbed = true
                    ).send()
                } else {
                    message.channel.createEmbed {
                        val command = getCommand(args[0])

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
     * Gather all available commands from the bot, and return them as an array of [KDCommand].
     */
    fun gatherCommands() = bot.commands.filter { !it.hidden && it.enabled }

    /**
     * Generate help message by formatting a [List] of [Command] objects.
     */
    fun formatMainHelp(commands: List<Command>): List<String> {
        return commands.sortedBy { it.name }.chunked(HELP_PER_PAGE).map { list ->
            list.joinToString(separator = "\n\n") { command ->
                with(command) {
                    "**${bot.prefix}$name $signature**\n${description.takeWhile { it != '\n' }}"
                }
            }
        }
    }

    /**
     * Return the [Command] of the associated name, or null if it cannot be found.
     */
    fun getCommand(command: String) = bot.commands.firstOrNull { it.name == command || it.aliases.contains(command) }

    /**
     * Format the given command's description into a short help string.
     *
     * @param command The command to format the description of.
     */
    fun formatLongHelp(command: Command): String {
        return "**${bot.prefix}${command.name} ${command.signature}**\n\n" +
                "*${command.description}*"
    }
}
