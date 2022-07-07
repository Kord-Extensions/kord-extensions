/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.commands.Arguments

private const val SUBCOMMAND_AND_GROUP_LIMIT: Int = 25

// region: Group creation

/**
 * Create a command group, using the given name.
 *
 * Note that only root/top-level commands can contain command groups. An error will be thrown if you try to use
 * this with a subcommand.
 *
 * @param name Name of the command group on Discord.
 * @param body Lambda used to build the [SlashGroup] object.
 */
public suspend fun SlashCommand<*, *>.group(name: String, body: suspend SlashGroup.() -> Unit): SlashGroup {
    if (parentCommand != null) {
        error("Command groups may not be nested inside subcommands.")
    }

    if (subCommands.isNotEmpty()) {
        error("Commands may only contain subcommands or command groups, not both.")
    }

    if (groups.size >= SUBCOMMAND_AND_GROUP_LIMIT) {
        error("Commands may only contain up to $SUBCOMMAND_AND_GROUP_LIMIT command groups.")
    }

    val localizedGroupName = localize(name, true).default

    if (groups[localizedGroupName] != null) {
        error("A command group with the name '$name' has already been registered.")
    }

    val group = SlashGroup(name, this)

    body(group)
    group.validate()

    groups[localizedGroupName] = group

    return group
}

// endregion

// region: Slash commands (Ephemeral)

/**
 * DSL function for easily registering an ephemeral subcommand, with arguments.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
public suspend fun <T : Arguments> SlashCommand<*, *>.ephemeralSubCommand(
    arguments: () -> T,
    body: suspend EphemeralSlashCommand<T>.() -> Unit
): EphemeralSlashCommand<T> {
    val commandObj = EphemeralSlashCommand(extension, arguments, parentCommand, parentGroup)
    body(commandObj)

    return ephemeralSubCommand(commandObj)
}

/**
 * Function for registering a custom ephemeral slash command object, for subcommands.
 *
 * You can use this if you have a custom ephemeral slash command subclass you need to register.
 *
 * @param commandObj EphemeralSlashCommand object to register as a subcommand.
 */
public fun <T : Arguments> SlashCommand<*, *>.ephemeralSubCommand(
    commandObj: EphemeralSlashCommand<T>
): EphemeralSlashCommand<T> {
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
 * DSL function for easily registering an ephemeral subcommand, without arguments.
 *
 * Use this in your slash command function to register a subcommand that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the subcommand object.
 */
public suspend fun SlashCommand<*, *>.ephemeralSubCommand(
    body: suspend EphemeralSlashCommand<Arguments>.() -> Unit
): EphemeralSlashCommand<Arguments> {
    val commandObj = EphemeralSlashCommand<Arguments>(extension, null, this, parentGroup)
    body(commandObj)

    return ephemeralSubCommand(commandObj)
}

// endregion

// region: Slash commands (Public)

/**
 * DSL function for easily registering a public subcommand, with arguments.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
public suspend fun <T : Arguments> SlashCommand<*, *>.publicSubCommand(
    arguments: () -> T,
    body: suspend PublicSlashCommand<T>.() -> Unit
): PublicSlashCommand<T> {
    val commandObj = PublicSlashCommand(extension, arguments, parentCommand, parentGroup)
    body(commandObj)

    return publicSubCommand(commandObj)
}

/**
 * Function for registering a custom public slash command object, for subcommands.
 *
 * You can use this if you have a custom public slash command subclass you need to register.
 *
 * @param commandObj PublicSlashCommand object to register as a subcommand.
 */
public fun <T : Arguments> SlashCommand<*, *>.publicSubCommand(
    commandObj: PublicSlashCommand<T>
): PublicSlashCommand<T> {
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
 * DSL function for easily registering a public subcommand, without arguments.
 *
 * Use this in your slash command function to register a subcommand that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the subcommand object.
 */
public suspend fun SlashCommand<*, *>.publicSubCommand(
    body: suspend PublicSlashCommand<Arguments>.() -> Unit
): PublicSlashCommand<Arguments> {
    val commandObj = PublicSlashCommand<Arguments>(extension, null, this, parentGroup)
    body(commandObj)

    return publicSubCommand(commandObj)
}

// endregion

// region: Slash groups (Ephemeral)

/**
 * DSL function for easily registering an ephemeral subcommand, with arguments.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
public suspend fun <T : Arguments> SlashGroup.ephemeralSubCommand(
    arguments: () -> T,
    body: suspend EphemeralSlashCommand<T>.() -> Unit
): EphemeralSlashCommand<T> {
    val commandObj = EphemeralSlashCommand(parent.extension, arguments, parent, this)
    body(commandObj)

    return ephemeralSubCommand(commandObj)
}

/**
 * Function for registering a custom ephemeral slash command object, for subcommands.
 *
 * You can use this if you have a custom ephemeral slash command subclass you need to register.
 *
 * @param commandObj EphemeralSlashCommand object to register as a subcommand.
 */
public fun <T : Arguments> SlashGroup.ephemeralSubCommand(
    commandObj: EphemeralSlashCommand<T>
): EphemeralSlashCommand<T> {
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
 * DSL function for easily registering an ephemeral subcommand, without arguments.
 *
 * Use this in your slash command function to register a subcommand that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the subcommand object.
 */
public suspend fun SlashGroup.ephemeralSubCommand(
    body: suspend EphemeralSlashCommand<Arguments>.() -> Unit
): EphemeralSlashCommand<Arguments> {
    val commandObj = EphemeralSlashCommand<Arguments>(parent.extension, null, parent, this)
    body(commandObj)

    return ephemeralSubCommand(commandObj)
}

// endregion

// region: Slash groups (Public)

/**
 * DSL function for easily registering a public subcommand, with arguments.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
public suspend fun <T : Arguments> SlashGroup.publicSubCommand(
    arguments: () -> T,
    body: suspend PublicSlashCommand<T>.() -> Unit
): PublicSlashCommand<T> {
    val commandObj = PublicSlashCommand(parent.extension, arguments, parent, this)
    body(commandObj)

    return publicSubCommand(commandObj)
}

/**
 * Function for registering a custom public slash command object, for subcommands.
 *
 * You can use this if you have a custom public slash command subclass you need to register.
 *
 * @param commandObj PublicSlashCommand object to register as a subcommand.
 */
public fun <T : Arguments> SlashGroup.publicSubCommand(
    commandObj: PublicSlashCommand<T>
): PublicSlashCommand<T> {
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
 * DSL function for easily registering a public subcommand, without arguments.
 *
 * Use this in your slash command function to register a subcommand that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the subcommand object.
 */
public suspend fun SlashGroup.publicSubCommand(
    body: suspend PublicSlashCommand<Arguments>.() -> Unit
): PublicSlashCommand<Arguments> {
    val commandObj = PublicSlashCommand<Arguments>(parent.extension, null, parent, this)
    body(commandObj)

    return publicSubCommand(commandObj)
}

// endregion
