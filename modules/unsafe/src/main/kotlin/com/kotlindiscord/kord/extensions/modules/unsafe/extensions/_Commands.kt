/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.modules.unsafe.extensions

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.commands.UnsafeMessageCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.commands.UnsafeSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.commands.UnsafeUserCommand
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

// region: Message commands

/** Register an unsafe message command, DSL-style. **/
@ExtensionDSL
@UnsafeAPI
public suspend fun Extension.unsafeMessageCommand(
    body: suspend UnsafeMessageCommand<ModalForm>.() -> Unit
): UnsafeMessageCommand<ModalForm> {
    val commandObj = UnsafeMessageCommand<ModalForm>(this)
    body(commandObj)

    return unsafeMessageCommand(commandObj)
}

/** Register a custom instance of an unsafe message command. **/
@ExtensionDSL
@UnsafeAPI
public suspend fun <M : ModalForm> Extension.unsafeMessageCommand(
    commandObj: UnsafeMessageCommand<M>
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
    body: suspend UnsafeSlashCommand<T, ModalForm>.() -> Unit
): UnsafeSlashCommand<T, ModalForm> {
    val commandObj = UnsafeSlashCommand<T, ModalForm>(this, arguments, null, null, null)
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
public suspend fun <T : Arguments, M : ModalForm> Extension.unsafeSlashCommand(
    commandObj: UnsafeSlashCommand<T, M>
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
    body: suspend UnsafeSlashCommand<Arguments, ModalForm>.() -> Unit
): UnsafeSlashCommand<Arguments, ModalForm> {
    val commandObj = UnsafeSlashCommand<Arguments, ModalForm>(this, null, null, null)
    body(commandObj)

    return unsafeSlashCommand(commandObj)
}

// endregion

// region: User commands

/** Register an unsafe user command, DSL-style. **/
@ExtensionDSL
@UnsafeAPI
public suspend fun Extension.unsafeUserCommand(
    body: suspend UnsafeUserCommand<ModalForm>.() -> Unit
): UnsafeUserCommand<ModalForm> {
    val commandObj = UnsafeUserCommand<ModalForm>(this)
    body(commandObj)

    return unsafeUserCommand(commandObj)
}

/** Register a custom instance of an unsafe user command. **/
@ExtensionDSL
@UnsafeAPI
public suspend fun <M : ModalForm> Extension.unsafeUserCommand(
    commandObj: UnsafeUserCommand<M>
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
