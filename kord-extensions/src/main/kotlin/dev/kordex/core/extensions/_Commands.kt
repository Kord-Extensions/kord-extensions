/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.core.extensions

import dev.kord.gateway.Intent
import dev.kordex.core.CommandRegistrationException
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.annotations.ExtensionDSL
import dev.kordex.core.checks.types.ChatCommandCheck
import dev.kordex.core.checks.types.MessageCommandCheck
import dev.kordex.core.checks.types.SlashCommandCheck
import dev.kordex.core.checks.types.UserCommandCheck
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.message.EphemeralMessageCommand
import dev.kordex.core.commands.application.message.PublicMessageCommand
import dev.kordex.core.commands.application.slash.EphemeralSlashCommand
import dev.kordex.core.commands.application.slash.PublicSlashCommand
import dev.kordex.core.commands.application.user.EphemeralUserCommand
import dev.kordex.core.commands.application.user.PublicUserCommand
import dev.kordex.core.commands.chat.ChatCommand
import dev.kordex.core.commands.chat.ChatGroupCommand
import dev.kordex.core.components.forms.ModalForm
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

// region: Message commands

/**
 * Define a check which must pass for a message command to be executed. This check will be applied to all
 * message commands in this extension.
 *
 * A message command may have multiple checks - all checks must pass for the command to be executed.
 * Checks will be run in the order that they're defined.
 *
 * This function can be used DSL-style with a given body, or it can be passed one or more
 * predefined functions. See the samples for more information.
 *
 * @param checks Checks to apply to all slash commands.
 */
public fun Extension.messageCommandCheck(vararg checks: MessageCommandCheck) {
	checks.forEach { messageCommandChecks.add(it) }
}

/**
 * Overloaded message command check function to allow for DSL syntax.
 *
 * @param check Check to apply to all slash commands.
 */
public fun Extension.messageCommandCheck(check: MessageCommandCheck) {
	messageCommandChecks.add(check)
}

/** Register an ephemeral message command, DSL-style. **/
@ExtensionDSL
public suspend fun Extension.ephemeralMessageCommand(
	body: suspend EphemeralMessageCommand<ModalForm>.() -> Unit,
): EphemeralMessageCommand<ModalForm> {
	val commandObj = EphemeralMessageCommand<ModalForm>(this)
	body(commandObj)

	return ephemeralMessageCommand(commandObj)
}

/** Register an ephemeral message command, DSL-style. **/
@ExtensionDSL
public suspend fun <M : ModalForm> Extension.ephemeralMessageCommand(
	modal: (() -> M),
	body: suspend EphemeralMessageCommand<M>.() -> Unit,
): EphemeralMessageCommand<M> {
	val commandObj = EphemeralMessageCommand(this, modal)
	body(commandObj)

	return ephemeralMessageCommand(commandObj)
}

/** Register a custom instance of an ephemeral message command. **/
@ExtensionDSL
public suspend fun <M : ModalForm> Extension.ephemeralMessageCommand(
	commandObj: EphemeralMessageCommand<M>,
): EphemeralMessageCommand<M> {
	try {
		commandObj.validate()
		messageCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
	} catch (e: InvalidCommandException) {
		logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
	}

	if (applicationCommandRegistry.initialised) {
		applicationCommandRegistry.register(commandObj)
	}

	return commandObj
}

/** Register a public message command, DSL-style. **/
@ExtensionDSL
public suspend fun Extension.publicMessageCommand(
	body: suspend PublicMessageCommand<ModalForm>.() -> Unit,
): PublicMessageCommand<ModalForm> {
	val commandObj = PublicMessageCommand<ModalForm>(this)
	body(commandObj)

	return publicMessageCommand(commandObj)
}

/** Register a public message command, DSL-style. **/
@ExtensionDSL
public suspend fun <M : ModalForm> Extension.publicMessageCommand(
	modal: (() -> M),
	body: suspend PublicMessageCommand<M>.() -> Unit,
): PublicMessageCommand<M> {
	val commandObj = PublicMessageCommand(this, modal)
	body(commandObj)

	return publicMessageCommand(commandObj)
}

/** Register a custom instance of a public message command. **/
@ExtensionDSL
public suspend fun <M : ModalForm> Extension.publicMessageCommand(
	commandObj: PublicMessageCommand<M>,
): PublicMessageCommand<M> {
	try {
		commandObj.validate()
		messageCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
	} catch (e: InvalidCommandException) {
		logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
	}

	if (applicationCommandRegistry.initialised) {
		applicationCommandRegistry.register(commandObj)
	}

	return commandObj
}

// endregion

// region: Slash commands (Generic)

/**
 * Define a check which must pass for a slash command to be executed. This check will be applied to all
 * slash commands in this extension.
 *
 * A slash command may have multiple checks - all checks must pass for the command to be executed.
 * Checks will be run in the order that they're defined.
 *
 * This function can be used DSL-style with a given body, or it can be passed one or more
 * predefined functions. See the samples for more information.
 *
 * @param checks Checks to apply to all slash commands.
 */
public fun Extension.slashCommandCheck(vararg checks: SlashCommandCheck) {
	checks.forEach { slashCommandChecks.add(it) }
}

/**
 * Overloaded slash command check function to allow for DSL syntax.
 *
 * @param check Check to apply to all slash commands.
 */
public fun Extension.slashCommandCheck(check: SlashCommandCheck) {
	slashCommandChecks.add(check)
}

// endregion

// region: Slash commands (Ephemeral)

/**
 * DSL function for easily registering an ephemeral slash command, with arguments.
 *
 * Use this in your setup function to register a slash command that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <T : Arguments> Extension.ephemeralSlashCommand(
	arguments: () -> T,
	body: suspend EphemeralSlashCommand<T, ModalForm>.() -> Unit,
): EphemeralSlashCommand<T, ModalForm> {
	val commandObj = EphemeralSlashCommand<T, ModalForm>(this, arguments, null, null)
	body(commandObj)

	return ephemeralSlashCommand(commandObj)
}

/**
 * DSL function for easily registering an ephemeral slash command, with a modal form.
 *
 * Use this in your setup function to register a slash command that may be executed on Discord.
 *
 * @param modal ModalForm builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
@JvmName("ephemeralSlashCommand1")
public suspend fun <M : ModalForm> Extension.ephemeralSlashCommand(
	modal: () -> M,
	body: suspend EphemeralSlashCommand<Arguments, M>.() -> Unit,
): EphemeralSlashCommand<Arguments, M> {
	val commandObj = EphemeralSlashCommand<Arguments, M>(
		this,
		null,
		modal,
		null,
		null
	)

	body(commandObj)

	return ephemeralSlashCommand(commandObj)
}

/**
 * DSL function for easily registering an ephemeral slash command, with a modal form.
 *
 * Use this in your setup function to register a slash command that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param modal ModalForm builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <A : Arguments, M : ModalForm> Extension.ephemeralSlashCommand(
	arguments: () -> A,
	modal: () -> M,
	body: suspend EphemeralSlashCommand<A, M>.() -> Unit,
): EphemeralSlashCommand<A, M> {
	val commandObj = EphemeralSlashCommand(
		this,
		arguments,
		modal,
		null,
		null
	)

	body(commandObj)

	return ephemeralSlashCommand(commandObj)
}

/**
 * Function for registering a custom ephemeral slash command object.
 *
 * You can use this if you have a custom ephemeral slash command subclass you need to register.
 *
 * @param commandObj EphemeralSlashCommand object to register.
 */
@ExtensionDSL
public suspend fun <T : Arguments, M : ModalForm> Extension.ephemeralSlashCommand(
	commandObj: EphemeralSlashCommand<T, M>,
): EphemeralSlashCommand<T, M> {
	try {
		commandObj.validate()
		slashCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		logger.error(e) { "Failed to register subcommand - $e" }
	} catch (e: InvalidCommandException) {
		logger.error(e) { "Failed to register subcommand - $e" }
	}

	if (applicationCommandRegistry.initialised) {
		applicationCommandRegistry.register(commandObj)
	}

	return commandObj
}

/**
 * DSL function for easily registering an ephemeral slash command, without arguments.
 *
 * Use this in your setup function to register a slash command that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun Extension.ephemeralSlashCommand(
	body: suspend EphemeralSlashCommand<Arguments, ModalForm>.() -> Unit,
): EphemeralSlashCommand<Arguments, ModalForm> {
	val commandObj = EphemeralSlashCommand<Arguments, ModalForm>(
		this,
		null,
		null,
		null,
		null
	)

	body(commandObj)

	return ephemeralSlashCommand(commandObj)
}

// endregion

// region: Slash commands (Public)

/**
 * DSL function for easily registering a public slash command, with arguments.
 *
 * Use this in your setup function to register a slash command that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <T : Arguments> Extension.publicSlashCommand(
	arguments: () -> T,
	body: suspend PublicSlashCommand<T, ModalForm>.() -> Unit,
): PublicSlashCommand<T, ModalForm> {
	val commandObj = PublicSlashCommand<T, ModalForm>(
		this,
		arguments,
		null,
		null,
		null
	)

	body(commandObj)

	return publicSlashCommand(commandObj)
}

/**
 * DSL function for easily registering a public slash command, with a modal form.
 *
 * Use this in your setup function to register a slash command that may be executed on Discord.
 *
 * @param modal ModalForm builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
@JvmName("publicSlashCommand1")
public suspend fun <M : ModalForm> Extension.publicSlashCommand(
	modal: () -> M,
	body: suspend PublicSlashCommand<Arguments, M>.() -> Unit,
): PublicSlashCommand<Arguments, M> {
	val commandObj = PublicSlashCommand<Arguments, M>(
		this,
		null,
		modal,
		null,
		null
	)

	body(commandObj)

	return publicSlashCommand(commandObj)
}

/**
 * DSL function for easily registering a public slash command, with arguments and a modal form.
 *
 * Use this in your setup function to register a slash command that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param modal ModalForm builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <A : Arguments, M : ModalForm> Extension.publicSlashCommand(
	arguments: () -> A,
	modal: () -> M,
	body: suspend PublicSlashCommand<A, M>.() -> Unit,
): PublicSlashCommand<A, M> {
	val commandObj = PublicSlashCommand(
		this,
		arguments,
		modal,
		null
	)

	body(commandObj)

	return publicSlashCommand(commandObj)
}

/**
 * Function for registering a custom public slash command object.
 *
 * You can use this if you have a custom public slash command subclass you need to register.
 *
 * @param commandObj PublicSlashCommand object to register.
 */
@ExtensionDSL
public suspend fun <T : Arguments, M : ModalForm> Extension.publicSlashCommand(
	commandObj: PublicSlashCommand<T, M>,
): PublicSlashCommand<T, M> {
	try {
		commandObj.validate()
		slashCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		logger.error(e) { "Failed to register subcommand - $e" }
	} catch (e: InvalidCommandException) {
		logger.error(e) { "Failed to register subcommand - $e" }
	}

	if (applicationCommandRegistry.initialised) {
		applicationCommandRegistry.register(commandObj)
	}

	return commandObj
}

/**
 * DSL function for easily registering a public slash command, without arguments.
 *
 * Use this in your setup function to register a slash command that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun Extension.publicSlashCommand(
	body: suspend PublicSlashCommand<Arguments, ModalForm>.() -> Unit,
): PublicSlashCommand<Arguments, ModalForm> {
	val commandObj = PublicSlashCommand<Arguments, ModalForm>(
		this,
		null,
		null,
		null,
		null
	)

	body(commandObj)

	return publicSlashCommand(commandObj)
}

// endregion

// region: User commands

/**
 * Define a check which must pass for a user command to be executed. This check will be applied to all
 * user commands in this extension.
 *
 * A user command may have multiple checks - all checks must pass for the command to be executed.
 * Checks will be run in the order that they're defined.
 *
 * This function can be used DSL-style with a given body, or it can be passed one or more
 * predefined functions. See the samples for more information.
 *
 * @param checks Checks to apply to all slash commands.
 */
public fun Extension.userCommandCheck(vararg checks: UserCommandCheck) {
	checks.forEach { userCommandChecks.add(it) }
}

/**
 * Overloaded user command check function to allow for DSL syntax.
 *
 * @param check Check to apply to all slash commands.
 */
public fun Extension.userCommandCheck(check: UserCommandCheck) {
	userCommandChecks.add(check)
}

/** Register an ephemeral user command, DSL-style. **/
@ExtensionDSL
public suspend fun Extension.ephemeralUserCommand(
	body: suspend EphemeralUserCommand<ModalForm>.() -> Unit,
): EphemeralUserCommand<ModalForm> {
	val commandObj = EphemeralUserCommand<ModalForm>(this)
	body(commandObj)

	return ephemeralUserCommand(commandObj)
}

/** Register an ephemeral user command, DSL-style. **/
@ExtensionDSL
public suspend fun <M : ModalForm> Extension.ephemeralUserCommand(
	modal: (() -> M),
	body: suspend EphemeralUserCommand<M>.() -> Unit,
): EphemeralUserCommand<M> {
	val commandObj = EphemeralUserCommand(this, modal)
	body(commandObj)

	return ephemeralUserCommand(commandObj)
}

/** Register a custom instance of an ephemeral user command. **/
@ExtensionDSL
public suspend fun <M : ModalForm> Extension.ephemeralUserCommand(
	commandObj: EphemeralUserCommand<M>,
): EphemeralUserCommand<M> {
	try {
		commandObj.validate()
		userCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
	} catch (e: InvalidCommandException) {
		logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
	}

	if (applicationCommandRegistry.initialised) {
		applicationCommandRegistry.register(commandObj)
	}

	return commandObj
}

/** Register a public user command, DSL-style. **/
@ExtensionDSL
public suspend fun Extension.publicUserCommand(
	body: suspend PublicUserCommand<ModalForm>.() -> Unit,
): PublicUserCommand<ModalForm> {
	val commandObj = PublicUserCommand<ModalForm>(this)
	body(commandObj)

	return publicUserCommand(commandObj)
}

/** Register a public user command, DSL-style. **/
@ExtensionDSL
public suspend fun <M : ModalForm> Extension.publicUserCommand(
	modal: (() -> M),
	body: suspend PublicUserCommand<M>.() -> Unit,
): PublicUserCommand<M> {
	val commandObj = PublicUserCommand(this, modal)
	body(commandObj)

	return publicUserCommand(commandObj)
}

/** Register a custom instance of a public user command. **/
@ExtensionDSL
public suspend fun <M : ModalForm> Extension.publicUserCommand(
	commandObj: PublicUserCommand<M>,
): PublicUserCommand<M> {
	try {
		commandObj.validate()
		userCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
	} catch (e: InvalidCommandException) {
		logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
	}

	if (applicationCommandRegistry.initialised) {
		applicationCommandRegistry.register(commandObj)
	}

	return commandObj
}

// endregion

// region: Chat commands

/**
 * Define a check which must pass for the command to be executed. This check will be applied to all commands
 * in this extension.
 *
 * A command may have multiple checks - all checks must pass for the command to be executed.
 * Checks will be run in the order that they're defined.
 *
 * This function can be used DSL-style with a given body, or it can be passed one or more
 * predefined functions. See the samples for more information.
 *
 * @param checks Checks to apply to all commands in this extension.
 */
@ExtensionDSL
public fun Extension.chatCommandCheck(vararg checks: ChatCommandCheck) {
	checks.forEach { chatCommandChecks.add(it) }
}

/**
 * Overloaded check function to allow for DSL syntax.
 *
 * @param check Check to apply to all commands in this extension.
 */
@ExtensionDSL
public fun Extension.chatCommandCheck(check: ChatCommandCheck) {
	chatCommandChecks.add(check)
}

/**
 * DSL function for easily registering a command.
 *
 * Use this in your setup function to register a command that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the command object.
 */
@ExtensionDSL
public suspend fun <T : Arguments> Extension.chatCommand(
	arguments: () -> T,
	body: suspend ChatCommand<T>.() -> Unit,
): ChatCommand<T> {
	val commandObj = ChatCommand(this, arguments)
	body.invoke(commandObj)

	return chatCommand(commandObj)
}

/**
 * DSL function for easily registering a command, without arguments.
 *
 * Use this in your setup function to register a command that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the command object.
 */
@ExtensionDSL
public suspend fun Extension.chatCommand(
	body: suspend ChatCommand<Arguments>.() -> Unit,
): ChatCommand<Arguments> {
	val commandObj = ChatCommand<Arguments>(this)
	body.invoke(commandObj)

	return chatCommand(commandObj)
}

/**
 * Function for registering a custom command object.
 *
 * You can use this if you have a custom command subclass you need to register.
 *
 * @param commandObj [ChatCommand] object to register.
 */
@ExtensionDSL
public fun <T : Arguments> Extension.chatCommand(
	commandObj: ChatCommand<T>,
): ChatCommand<T> {
	try {
		commandObj.validate()
		chatCommandRegistry.add(commandObj)
		chatCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		logger.error(e) { "Failed to register command - $e" }
	} catch (e: InvalidCommandException) {
		logger.error(e) { "Failed to register command - $e" }
	}

	if (chatCommandRegistry.enabled) {  // Don't add the intents if they won't be used
		intents += Intent.DirectMessages
		intents += Intent.GuildMessages
	}

	return commandObj
}

/**
 * DSL function for easily registering a grouped command.
 *
 * Use this in your setup function to register a group of commands.
 *
 * The body of the grouped command will be executed if there is no
 * matching subcommand.
 *
 * @param body Builder lambda used for setting up the command object.
 */
@ExtensionDSL
public suspend fun <T : Arguments> Extension.chatGroupCommand(
	arguments: () -> T,
	body: suspend ChatGroupCommand<T>.() -> Unit,
): ChatGroupCommand<T> {
	val commandObj = ChatGroupCommand(this, arguments)
	body.invoke(commandObj)

	return chatCommand(commandObj) as ChatGroupCommand
}

/**
 * DSL function for easily registering a grouped command, without its own arguments.
 *
 * Use this in your setup function to register a group of commands.
 *
 * The body of the grouped command will be executed if there is no
 * matching subcommand.
 *
 * @param body Builder lambda used for setting up the command object.
 */
@ExtensionDSL
public suspend fun Extension.chatGroupCommand(
	body: suspend ChatGroupCommand<Arguments>.() -> Unit,
): ChatGroupCommand<Arguments> {
	val commandObj = ChatGroupCommand<Arguments>(this)
	body.invoke(commandObj)

	return chatCommand(commandObj) as ChatGroupCommand
}

// endregion
