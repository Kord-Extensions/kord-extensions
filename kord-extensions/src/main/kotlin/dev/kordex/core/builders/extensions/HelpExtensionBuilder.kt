/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders.extensions

import dev.kord.common.Color
import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.annotations.BotBuilderDSL
import dev.kordex.core.checks.types.ChatCommandCheck

/** Builder used for configuring options, specifically related to the help extension. **/
@BotBuilderDSL
public open class HelpExtensionBuilder {
	/** Whether to enable the bundled help extension. Defaults to `true`. **/
	public var enableBundledExtension: Boolean = true

	/**
	 * Time to wait before the help paginator times out and can't be used, in seconds. Defaults to 60.
	 */
	@Suppress("MagicNumber")
	public var paginatorTimeout: Long = 60L  // 60 seconds

	/** Whether to delete the help paginator after the timeout ends. **/
	public var deletePaginatorOnTimeout: Boolean = false

	/** Whether to delete the help command invocation after the paginator timeout ends. **/
	public var deleteInvocationOnPaginatorTimeout: Boolean = false

	/** Whether to ping users when responding to them. **/
	public var pingInReply: Boolean = true

	/** List of command checks. These checks will be checked against all commands in the help extension. **/
	public val checkList: MutableList<ChatCommandCheck> = mutableListOf()

	/** For custom help embed colours. Only one may be defined. **/
	public var colourGetter: suspend MessageCreateEvent.() -> Color = { DISCORD_BLURPLE }

	/** Define a callback that returns a [Color] to use for help embed colours. Feel free to mix it up! **/
	public fun colour(builder: suspend MessageCreateEvent.() -> Color) {
		colourGetter = builder
	}

	/** Like [colour], but American. **/
	public fun color(builder: suspend MessageCreateEvent.() -> Color): Unit = colour(builder)

	/**
	 * Define a check which must pass for help commands to be executed. This check will be applied to all
	 * commands in the extension.
	 *
	 * A command may have multiple checks - all checks must pass for the command to be executed.
	 * Checks will be run in the order that they're defined.
	 *
	 * This function can be used DSL-style with a given body, or it can be passed one or more
	 * predefined functions. See the samples for more information.
	 *
	 * @param checks Checks to apply to all help commands.
	 */
	public fun check(vararg checks: ChatCommandCheck) {
		checks.forEach { checkList.add(it) }
	}

	/**
	 * Overloaded check function to allow for DSL syntax.
	 *
	 * @param check Check to apply to all help commands.
	 */
	public fun check(check: ChatCommandCheck) {
		checkList.add(check)
	}
}
