/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.modules.dev.unsafe.extensions

import dev.kordex.core.CommandRegistrationException
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.SlashCommand
import dev.kordex.core.commands.application.slash.SlashGroup
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.UnsafeSlashCommand

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
	body: suspend UnsafeSlashCommand<T, ModalForm>.() -> Unit,
): UnsafeSlashCommand<T, ModalForm> {
	val commandObj = UnsafeSlashCommand<T, ModalForm>(extension, arguments, null, this, parentGroup)
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
public fun <T : Arguments, M : ModalForm> SlashCommand<*, *, *>.unsafeSubCommand(
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
	body: suspend UnsafeSlashCommand<Arguments, ModalForm>.() -> Unit,
): UnsafeSlashCommand<Arguments, ModalForm> {
	val commandObj = UnsafeSlashCommand<Arguments, ModalForm>(extension, null, null, this, parentGroup)
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
	body: suspend UnsafeSlashCommand<T, ModalForm>.() -> Unit,
): UnsafeSlashCommand<T, ModalForm> {
	val commandObj = UnsafeSlashCommand<T, ModalForm>(parent.extension, arguments, null, parent, this)
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
public fun <T : Arguments, M : ModalForm> SlashGroup.unsafeSubCommand(
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
	body: suspend UnsafeSlashCommand<Arguments, ModalForm>.() -> Unit,
): UnsafeSlashCommand<Arguments, ModalForm> {
	val commandObj = UnsafeSlashCommand<Arguments, ModalForm>(parent.extension, null, null, parent, this)
	body(commandObj)

	return unsafeSubCommand(commandObj)
}

// endregion
