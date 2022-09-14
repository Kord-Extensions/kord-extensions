/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.modules.unsafe.extensions

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.commands.UnsafeSlashCommand

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
public suspend fun <T : Arguments> SlashCommand<*, *>.unsafeSubCommand(
    arguments: () -> T,
    body: suspend UnsafeSlashCommand<T>.() -> Unit
): UnsafeSlashCommand<T> {
    val commandObj = UnsafeSlashCommand(extension, arguments, this, parentGroup)
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
public fun <T : Arguments> SlashCommand<*, *>.unsafeSubCommand(
    commandObj: UnsafeSlashCommand<T>
): UnsafeSlashCommand<T> {
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
public suspend fun SlashCommand<*, *>.unsafeSubCommand(
    body: suspend UnsafeSlashCommand<Arguments>.() -> Unit
): UnsafeSlashCommand<Arguments> {
    val commandObj = UnsafeSlashCommand<Arguments>(extension, null, this, parentGroup)
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
    body: suspend UnsafeSlashCommand<T>.() -> Unit
): UnsafeSlashCommand<T> {
    val commandObj = UnsafeSlashCommand(parent.extension, arguments, parent, this)
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
public fun <T : Arguments> SlashGroup.unsafeSubCommand(
    commandObj: UnsafeSlashCommand<T>
): UnsafeSlashCommand<T> {
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
    body: suspend UnsafeSlashCommand<Arguments>.() -> Unit
): UnsafeSlashCommand<Arguments> {
    val commandObj = UnsafeSlashCommand<Arguments>(parent.extension, null, parent, this)
    body(commandObj)

    return unsafeSubCommand(commandObj)
}

// endregion
