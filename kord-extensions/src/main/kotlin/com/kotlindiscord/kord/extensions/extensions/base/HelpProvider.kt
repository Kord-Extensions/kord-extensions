package com.kotlindiscord.kord.extensions.extensions.base

import com.kotlindiscord.kord.extensions.commands.MessageCommand
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.pagination.Paginator
import dev.kord.core.event.message.MessageCreateEvent

/**
 * Interface representing the main functionality required for extensions that replace the bundled `HelpExtension`.
 *
 * This is a fairly rigid interface to try to ensure that users are given a consistent user experience. If this is too
 * restrictive, please open an issue on GitHub and chat to us!
 *
 * **Note:** There are expected behaviours from functions that implement this interface. Please read the doc comments
 * before writing your own implementation.
 */
public interface HelpProvider {
    /**
     * Given a command object and command prefix string, return a triple representing the formatted command name and
     * signature, formatted command description and formatted argument list.
     *
     * @param prefix Command prefix character to use while formatting.
     * @param event MessageCreateEvent that triggered this help invocation, used to run subcommand checks.
     * @param command Command object to format the help for.
     * @param longDescription Whether to include more than the first line of the command description, `false` by
     *        default.
     *
     * @return Tripe containing three formatted elements - the command's name and signature with prefix, the command's
     *         description, and the command's argument list.
     */
    public suspend fun formatCommandHelp(
        prefix: String,
        event: MessageCreateEvent,
        command: MessageCommand<out Arguments>,
        longDescription: Boolean = false
    ): Triple<String, String, String>

    /**
     * Gather all available commands (with passing checks) from the bot, and return them.
     */
    public suspend fun gatherCommands(event: MessageCreateEvent): List<MessageCommand<out Arguments>>

    /**
     * Return the [MessageCommand] specified in the arguments, or `null` if it can't be found (or the checks fail).
     */
    public suspend fun getCommand(event: MessageCreateEvent, args: List<String>): MessageCommand<out Arguments>?

    /**
     * Given an event, prefix and argument list, attempt to find the command represented by the arguments and return
     * a [Paginator], ready to be sent.
     *
     * The [Paginator] will contain an error message if the command can't be found, or the command's checks fail.
     *
     * @param event MessageCreateEvent that triggered this help invocation.
     * @param prefix Command prefix to use for formatting.
     * @param args List of arguments to use to find the command.
     *
     * @return Paginator containing the command's help, or an error message.
     */
    public suspend fun getCommandHelpPaginator(
        event: MessageCreateEvent,
        prefix: String,
        args: List<String>
    ): Paginator

    /**
     * Given an event, prefix and argument list, attempt to find the command represented by the arguments and return
     * a [Paginator], ready to be sent.
     *
     * The [Paginator] will contain an error message if the command passed was `null`, or the command's checks fail.
     *
     * Please be mindful of using this with subcommands, as the extension's design intends for users to be unable to
     * retrieve help for subcommands when any parent command's checks fail, and this function does not run those checks.
     *
     * @param event MessageCreateEvent that triggered this help invocation.
     * @param prefix Command prefix to use for formatting.
     * @param command Command object to format the help for.
     *
     * @return Paginator containing the command's help, or an error message.
     */
    public suspend fun getCommandHelpPaginator(
        event: MessageCreateEvent,
        prefix: String,
        command: MessageCommand<out Arguments>?
    ): Paginator

    /**
     * Given an event and prefix, return a [Paginator] containing help information for all loaded commands with passing
     * checks.
     *
     * While it shouldn't really be possible, this will also handle the case where there are no commands registered
     * at all, for all you weirdos out there breaking everything intentionally.
     *
     * This will only return help information for commands with checks that pass. If a command's checks fail, it will
     * not be listed. Similarly, a command will only show subcommands with passing checks.
     *
     * @param event MessageCreateEvent that triggered this help invocation.
     * @param prefix Command prefix to use for formatting.
     *
     * @return Paginator containing help information for all loaded commands with passing checks.
     */
    public suspend fun getMainHelpPaginator(event: MessageCreateEvent, prefix: String): Paginator
}
