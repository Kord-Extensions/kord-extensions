/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.modules.dev.unsafe.extensions

import dev.kordex.core.CommandRegistrationException
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.annotations.ExtensionDSL
import dev.kordex.core.commands.Arguments
import dev.kordex.core.extensions.Extension
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.message.UnsafeMessageCommand
import dev.kordex.modules.dev.unsafe.commands.slash.UnsafeSlashCommand
import dev.kordex.modules.dev.unsafe.commands.user.UnsafeUserCommand
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

// region: Message commands

/** Register an unsafe message command, DSL-style. **/
@ExtensionDSL
@UnsafeAPI
public suspend fun Extension.unsafeMessageCommand(
	body: suspend UnsafeMessageCommand<UnsafeModalForm>.() -> Unit,
): UnsafeMessageCommand<UnsafeModalForm> {
	val commandObj = UnsafeMessageCommand<UnsafeModalForm>(this)
	body(commandObj)

	return unsafeMessageCommand(commandObj)
}

/** Register a custom instance of an unsafe message command. **/
@ExtensionDSL
@UnsafeAPI
public suspend fun <M : UnsafeModalForm> Extension.unsafeMessageCommand(
    commandObj: UnsafeMessageCommand<M>,
): UnsafeMessageCommand<M> {
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

// region: Slash commands (Unsafe)

/**
 * DSL function for easily registering an unsafe slash command, with arguments.
 *
 * Use this in your setup function to register a slash command that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
@UnsafeAPI
public suspend fun <T : Arguments> Extension.unsafeSlashCommand(
	arguments: () -> T,
	body: suspend UnsafeSlashCommand<T, UnsafeModalForm>.() -> Unit,
): UnsafeSlashCommand<T, UnsafeModalForm> {
	val commandObj = UnsafeSlashCommand<T, UnsafeModalForm>(this, arguments, null, null, null)
	body(commandObj)

	return unsafeSlashCommand(commandObj)
}

/**
 * Function for registering a custom unsafe slash command object.
 *
 * You can use this if you have a custom unsafe slash command subclass you need to register.
 *
 * @param commandObj UnsafeSlashCommand object to register.
 */
@ExtensionDSL
@UnsafeAPI
public suspend fun <T : Arguments, M : UnsafeModalForm> Extension.unsafeSlashCommand(
	commandObj: UnsafeSlashCommand<T, M>,
): UnsafeSlashCommand<T, M> {
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
 * DSL function for easily registering an unsafe slash command, without arguments.
 *
 * Use this in your setup function to register a slash command that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
@UnsafeAPI
public suspend fun Extension.unsafeSlashCommand(
	body: suspend UnsafeSlashCommand<Arguments, UnsafeModalForm>.() -> Unit,
): UnsafeSlashCommand<Arguments, UnsafeModalForm> {
	val commandObj = UnsafeSlashCommand<Arguments, UnsafeModalForm>(this, null, null, null)
	body(commandObj)

	return unsafeSlashCommand(commandObj)
}

// endregion

// region: User commands

/** Register an unsafe user command, DSL-style. **/
@ExtensionDSL
@UnsafeAPI
public suspend fun Extension.unsafeUserCommand(
	body: suspend UnsafeUserCommand<UnsafeModalForm>.() -> Unit,
): UnsafeUserCommand<UnsafeModalForm> {
	val commandObj = UnsafeUserCommand<UnsafeModalForm>(this)
	body(commandObj)

	return unsafeUserCommand(commandObj)
}

/** Register a custom instance of an unsafe user command. **/
@ExtensionDSL
@UnsafeAPI
public suspend fun <M : UnsafeModalForm> Extension.unsafeUserCommand(
	commandObj: UnsafeUserCommand<M>,
): UnsafeUserCommand<M> {
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
