/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders

import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.annotations.BotBuilderDSL
import dev.kordex.core.checks.types.ChatCommandCheck
import dev.kordex.core.commands.chat.ChatCommandRegistry

/** Builder used for configuring the bot's chat command options. **/
@BotBuilderDSL
public class ChatCommandsBuilder {
	/** Whether to invoke commands on bot mentions, in addition to using chat prefixes. Defaults to `true`. **/
	public var invokeOnMention: Boolean = true

	/** Prefix to require for command invocations on Discord. Defaults to `"!"`. **/
	public var defaultPrefix: String = "!"

	/** Whether to register and process chat commands. Defaults to `false`. **/
	public var enabled: Boolean = false

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public var prefixCallback: suspend (MessageCreateEvent).(String) -> String = { defaultPrefix }

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public var registryBuilder: () -> ChatCommandRegistry = { ChatCommandRegistry() }

	/** Whether to ignore command invocations in messages sent by the bot. Defaults to `true`. **/
	public var ignoreSelf: Boolean = true

	/**
	 * List of command checks.
	 *
	 * These checks will be checked against all commands.
	 */
	public val checkList: MutableList<ChatCommandCheck> = mutableListOf()

	/**
	 * Register a lambda that takes a [MessageCreateEvent] object and the default prefix, and returns the
	 * command prefix to be made use of for that message event.
	 *
	 * This is intended to allow for different chat command prefixes in different contexts - for example,
	 * guild-specific prefixes.
	 */
	public fun prefix(builder: suspend (MessageCreateEvent).(String) -> String) {
		prefixCallback = builder
	}

	/**
	 * Register the builder used to create the [ChatCommandRegistry]. You can change this if you need to
	 * make use of a subclass.
	 */
	public fun registry(builder: () -> ChatCommandRegistry) {
		registryBuilder = builder
	}

	/**
	 * Define a check which must pass for the command to be executed. This check will be applied to all commands.
	 *
	 * A command may have multiple checks - all checks must pass for the command to be executed.
	 * Checks will be run in the order that they're defined.
	 *
	 * This function can be used DSL-style with a given body, or it can be passed one or more
	 * predefined functions. See the samples for more information.
	 *
	 * @param checks Checks to apply to all commands.
	 */
	public fun check(vararg checks: ChatCommandCheck) {
		checks.forEach { checkList.add(it) }
	}

	/**
	 * Overloaded check function to allow for DSL syntax.
	 *
	 * @param check Checks to apply to all commands.
	 */
	public fun check(check: ChatCommandCheck) {
		checkList.add(check)
	}
}
