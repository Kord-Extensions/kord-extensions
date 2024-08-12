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
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.SlashCommand
import dev.kordex.core.commands.application.slash.SlashGroup
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.slash.UnsafeSlashCommand
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm

private const val SUBCOMMAND_AND_GROUP_LIMIT: Int = 25

// region: Slash commands (Unsafe)

/**
 * DSL function for easily registering an unsafe subcommand, with arguments.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@UnsafeAPI
public suspend fun <T : Arguments> SlashCommand<*, *, *>.unsafeSubCommand(
	arguments: () -> T,
	body: suspend UnsafeSlashCommand<T, UnsafeModalForm>.() -> Unit,
): UnsafeSlashCommand<T, UnsafeModalForm> {
	val commandObj = UnsafeSlashCommand<T, UnsafeModalForm>(extension, arguments, null, this, parentGroup)
	body(commandObj)

	return unsafeSubCommand(commandObj)
}

/**
 * Function for registering a custom unsafe slash command object, for subcommands.
 *
 * You can use this if you have a custom unsafe slash command subclass you need to register.
 *
 * @param commandObj UnsafeSlashCommand object to register as a subcommand.
 */
@UnsafeAPI
public fun <T : Arguments, M : UnsafeModalForm> SlashCommand<*, *, *>.unsafeSubCommand(
    commandObj: UnsafeSlashCommand<T, M>,
): UnsafeSlashCommand<T, M> {
	commandObj.guildId = null
	if (subCommands.size >= SUBCOMMAND_AND_GROUP_LIMIT) {
		throw InvalidCommandException(
			commandObj.name,
			"Groups may only contain up to $SUBCOMMAND_AND_GROUP_LIMIT commands."
		)
	}

	try {
		commandObj.validate()
		subCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		kxLogger.error(e) { "Failed to register subcommand - $e" }
	} catch (e: InvalidCommandException) {
		kxLogger.error(e) { "Failed to register subcommand - $e" }
	}

	return commandObj
}

/**
 * DSL function for easily registering an unsafe subcommand, without arguments.
 *
 * Use this in your slash command function to register a subcommand that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the subcommand object.
 */
@UnsafeAPI
public suspend fun SlashCommand<*, *, *>.unsafeSubCommand(
	body: suspend UnsafeSlashCommand<Arguments, UnsafeModalForm>.() -> Unit,
): UnsafeSlashCommand<Arguments, UnsafeModalForm> {
	val commandObj = UnsafeSlashCommand<Arguments, UnsafeModalForm>(extension, null, null, this, parentGroup)
	body(commandObj)

	return unsafeSubCommand(commandObj)
}

// endregion

// region: Slash groups (Unsafe)

/**
 * DSL function for easily registering an unsafe subcommand, with arguments.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@UnsafeAPI
public suspend fun <T : Arguments> SlashGroup.unsafeSubCommand(
	arguments: () -> T,
	body: suspend UnsafeSlashCommand<T, UnsafeModalForm>.() -> Unit,
): UnsafeSlashCommand<T, UnsafeModalForm> {
	val commandObj = UnsafeSlashCommand<T, UnsafeModalForm>(parent.extension, arguments, null, parent, this)
	body(commandObj)

	return unsafeSubCommand(commandObj)
}

/**
 * Function for registering a custom unsafe slash command object, for subcommands.
 *
 * You can use this if you have a custom unsafe slash command subclass you need to register.
 *
 * @param commandObj UnsafeSlashCommand object to register as a subcommand.
 */
@UnsafeAPI
public fun <T : Arguments, M : UnsafeModalForm> SlashGroup.unsafeSubCommand(
    commandObj: UnsafeSlashCommand<T, M>,
): UnsafeSlashCommand<T, M> {
	commandObj.guildId = null

	if (subCommands.size >= SUBCOMMAND_AND_GROUP_LIMIT) {
		throw InvalidCommandException(
			commandObj.name,
			"Groups may only contain up to $SUBCOMMAND_AND_GROUP_LIMIT commands."
		)
	}

	try {
		commandObj.validate()
		subCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		logger.error(e) { "Failed to register subcommand - $e" }
	} catch (e: InvalidCommandException) {
		logger.error(e) { "Failed to register subcommand - $e" }
	}

	return commandObj
}

/**
 * DSL function for easily registering an unsafe subcommand, without arguments.
 *
 * Use this in your slash command function to register a subcommand that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the subcommand object.
 */
@UnsafeAPI
public suspend fun SlashGroup.unsafeSubCommand(
	body: suspend UnsafeSlashCommand<Arguments, UnsafeModalForm>.() -> Unit,
): UnsafeSlashCommand<Arguments, UnsafeModalForm> {
	val commandObj = UnsafeSlashCommand<Arguments, UnsafeModalForm>(parent.extension, null, null, parent, this)
	body(commandObj)

	return unsafeSubCommand(commandObj)
}

// endregion
