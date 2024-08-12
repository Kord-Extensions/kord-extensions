/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.extensions.base

import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.chat.ChatCommand
import dev.kordex.core.commands.chat.ChatCommandContext
import dev.kordex.core.commands.chat.ChatCommandRegistry
import dev.kordex.core.pagination.BasePaginator
import dev.kordex.core.utils.getKoin

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
		command: ChatCommand<out Arguments>,
		longDescription: Boolean = false,
	): Triple<String, String, String>

	/**
	 * Given a command object and command context, return a triple representing the formatted command name and
	 * signature, formatted command description and formatted argument list.
	 *
	 * @param context MessageCommandContext object that triggered this help invocation.
	 * @param command Command object to format the help for.
	 * @param longDescription Whether to include more than the first line of the command description, `false` by
	 *        default.
	 *
	 * @return Tripe containing three formatted elements - the command's name and signature with prefix, the command's
	 *         description, and the command's argument list.
	 */
	public suspend fun formatCommandHelp(
		context: ChatCommandContext<*>,
		command: ChatCommand<out Arguments>,
		longDescription: Boolean = false,
	): Triple<String, String, String> {
		val prefix = getKoin().get<ChatCommandRegistry>().getPrefix(context.event)

		return formatCommandHelp(prefix, context.event, command, longDescription)
	}

	/**
	 * Gather all available commands (with passing checks) from the bot, and return them.
	 */
	public suspend fun gatherCommands(event: MessageCreateEvent): List<ChatCommand<out Arguments>>

	/**
	 * Return the [MessageCommand] specified in the arguments, or `null` if it can't be found (or the checks fail).
	 */
	public suspend fun getCommand(event: MessageCreateEvent, args: List<String>): ChatCommand<out Arguments>?

	/**
	 * Given an event, prefix and argument list, attempt to find the command represented by the arguments and return
	 * a [BasePaginator], ready to be sent.
	 *
	 * The [BasePaginator] will contain an error message if the command can't be found, or the command's checks fail.
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
		args: List<String>,
	): BasePaginator

	/**
	 * Given a command context and argument list, attempt to find the command represented by the arguments and
	 * return a [BasePaginator], ready to be sent.
	 *
	 * The [BasePaginator] will contain an error message if the command can't be found, or the command's checks fail.
	 *
	 * @param context MessageCommandContext object that triggered this help invocation.
	 * @param args List of arguments to use to find the command.
	 *
	 * @return Paginator containing the command's help, or an error message.
	 */
	public suspend fun getCommandHelpPaginator(
		context: ChatCommandContext<*>,
		args: List<String>,
	): BasePaginator {
		val prefix = getKoin().get<ChatCommandRegistry>().getPrefix(context.event)

		return getCommandHelpPaginator(context.event, prefix, args)
	}

	/**
	 * Given an event, prefix and argument list, attempt to find the command represented by the arguments and return
	 * a [BasePaginator], ready to be sent.
	 *
	 * The [BasePaginator] will contain an error message if the command passed was `null`, or the command's checks fail.
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
		command: ChatCommand<out Arguments>?,
	): BasePaginator

	/**
	 * Given a command context and argument list, attempt to find the command represented by the arguments and return
	 * a [BasePaginator], ready to be sent.
	 *
	 * The [BasePaginator] will contain an error message if the command passed was `null`, or the command's checks fail.
	 *
	 * Please be mindful of using this with subcommands, as the extension's design intends for users to be unable to
	 * retrieve help for subcommands when any parent command's checks fail, and this function does not run those checks.
	 *
	 * @param context MessageCommandContext object that triggered this help invocation.
	 * @param command Command object to format the help for.
	 *
	 * @return Paginator containing the command's help, or an error message.
	 */
	public suspend fun getCommandHelpPaginator(
		context: ChatCommandContext<*>,
		command: ChatCommand<out Arguments>?,
	): BasePaginator {
		val prefix = getKoin().get<ChatCommandRegistry>().getPrefix(context.event)

		return getCommandHelpPaginator(context.event, prefix, command)
	}

	/**
	 * Given an event and prefix, return a [BasePaginator] containing help information for all loaded commands with
	 * passing checks.
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
	public suspend fun getMainHelpPaginator(event: MessageCreateEvent, prefix: String): BasePaginator

	/**
	 * Given a command context, return a [BasePaginator] containing help information for all loaded commands with
	 * passing checks.
	 *
	 * While it shouldn't really be possible, this will also handle the case where there are no commands registered
	 * at all, for all you weirdos out there breaking everything intentionally.
	 *
	 * This will only return help information for commands with checks that pass. If a command's checks fail, it will
	 * not be listed. Similarly, a command will only show subcommands with passing checks.
	 *
	 * @param context MessageCommandContext object that triggered this help invocation..
	 *
	 * @return BasePaginator containing help information for all loaded commands with passing checks.
	 */
	public suspend fun getMainHelpPaginator(context: ChatCommandContext<*>): BasePaginator {
		val prefix = getKoin().get<ChatCommandRegistry>().getPrefix(context.event)

		return getMainHelpPaginator(context.event, prefix)
	}
}
