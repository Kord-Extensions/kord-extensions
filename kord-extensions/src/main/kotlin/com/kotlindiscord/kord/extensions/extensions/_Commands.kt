@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.extensions

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.message.EphemeralMessageCommand
import com.kotlindiscord.kord.extensions.commands.application.message.PublicMessageCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.user.EphemeralUserCommand
import com.kotlindiscord.kord.extensions.commands.application.user.PublicUserCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatGroupCommand
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

// region: Message commands

/** Register an ephemeral message command, DSL-style. **/
public suspend fun Extension.ephemeralMessageCommand(
    body: suspend EphemeralMessageCommand.() -> Unit
): EphemeralMessageCommand {
    val commandObj = EphemeralMessageCommand(this)
    body(commandObj)

    return ephemeralMessageCommand(commandObj)
}

/** Register a custom instance of an ephemeral message command. **/
public fun Extension.ephemeralMessageCommand(
    commandObj: EphemeralMessageCommand
): EphemeralMessageCommand {
    try {
        commandObj.validate()
        messageCommands.add(commandObj)
    } catch (e: CommandRegistrationException) {
        logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
    } catch (e: InvalidCommandException) {
        logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
    }

    return commandObj
}

/** Register a public message command, DSL-style. **/
public suspend fun Extension.publicMessageCommand(
    body: suspend PublicMessageCommand.() -> Unit
): PublicMessageCommand {
    val commandObj = PublicMessageCommand(this)
    body(commandObj)

    return publicMessageCommand(commandObj)
}

/** Register a custom instance of a public message command. **/
public fun Extension.publicMessageCommand(
    commandObj: PublicMessageCommand
): PublicMessageCommand {
    try {
        commandObj.validate()
        messageCommands.add(commandObj)
    } catch (e: CommandRegistrationException) {
        logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
    } catch (e: InvalidCommandException) {
        logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
    }

    return commandObj
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
public suspend fun <T : Arguments> Extension.ephemeralSlashCommand(
    arguments: () -> T,
    body: suspend EphemeralSlashCommand<T>.() -> Unit
): EphemeralSlashCommand<T> {
    val commandObj = EphemeralSlashCommand(this, arguments, null, null)
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
public fun <T : Arguments> Extension.ephemeralSlashCommand(
    commandObj: EphemeralSlashCommand<T>
): EphemeralSlashCommand<T> {
    try {
        commandObj.validate()
        slashCommands.add(commandObj)
    } catch (e: CommandRegistrationException) {
        logger.error(e) { "Failed to register subcommand - $e" }
    } catch (e: InvalidCommandException) {
        logger.error(e) { "Failed to register subcommand - $e" }
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
public suspend fun Extension.ephemeralSlashCommand(
    body: suspend EphemeralSlashCommand<Arguments>.() -> Unit
): EphemeralSlashCommand<Arguments> {
    val commandObj = EphemeralSlashCommand<Arguments>(this, null, null, null)
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
public suspend fun <T : Arguments> Extension.publicSlashCommand(
    arguments: () -> T,
    body: suspend PublicSlashCommand<T>.() -> Unit
): PublicSlashCommand<T> {
    val commandObj = PublicSlashCommand(this, arguments, null, null)
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
public fun <T : Arguments> Extension.publicSlashCommand(
    commandObj: PublicSlashCommand<T>
): PublicSlashCommand<T> {
    try {
        commandObj.validate()
        slashCommands.add(commandObj)
    } catch (e: CommandRegistrationException) {
        logger.error(e) { "Failed to register subcommand - $e" }
    } catch (e: InvalidCommandException) {
        logger.error(e) { "Failed to register subcommand - $e" }
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
public suspend fun Extension.publicSlashCommand(
    body: suspend PublicSlashCommand<Arguments>.() -> Unit
): PublicSlashCommand<Arguments> {
    val commandObj = PublicSlashCommand<Arguments>(this, null, null, null)
    body(commandObj)

    return publicSlashCommand(commandObj)
}

// endregion

// region: User commands

/** Register an ephemeral user command, DSL-style. **/
public suspend fun Extension.ephemeralUserCommand(
    body: suspend EphemeralUserCommand.() -> Unit
): EphemeralUserCommand {
    val commandObj = EphemeralUserCommand(this)
    body(commandObj)

    return ephemeralUserCommand(commandObj)
}

/** Register a custom instance of an ephemeral user command. **/
public fun Extension.ephemeralUserCommand(
    commandObj: EphemeralUserCommand
): EphemeralUserCommand {
    try {
        commandObj.validate()
        userCommands.add(commandObj)
    } catch (e: CommandRegistrationException) {
        logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
    } catch (e: InvalidCommandException) {
        logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
    }

    return commandObj
}

/** Register a public user command, DSL-style. **/
public suspend fun Extension.publicUserCommand(
    body: suspend PublicUserCommand.() -> Unit
): PublicUserCommand {
    val commandObj = PublicUserCommand(this)
    body(commandObj)

    return publicUserCommand(commandObj)
}

/** Register a custom instance of a public user command. **/
public fun Extension.publicUserCommand(
    commandObj: PublicUserCommand
): PublicUserCommand {
    try {
        commandObj.validate()
        userCommands.add(commandObj)
    } catch (e: CommandRegistrationException) {
        logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
    } catch (e: InvalidCommandException) {
        logger.error(e) { "Failed to register message command ${commandObj.name} - $e" }
    }

    return commandObj
}

// endregion

// region: Chat commands

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
    body: suspend ChatCommand<T>.() -> Unit
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
    body: suspend ChatCommand<Arguments>.() -> Unit
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
 * @param commandObj MessageContentCommand object to register.
 */
public fun <T : Arguments> Extension.chatCommand(
    commandObj: ChatCommand<T>
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
    body: suspend ChatGroupCommand<T>.() -> Unit
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
    body: suspend ChatGroupCommand<Arguments>.() -> Unit
): ChatGroupCommand<Arguments> {
    val commandObj = ChatGroupCommand<Arguments>(this)
    body.invoke(commandObj)

    return chatCommand(commandObj) as ChatGroupCommand
}

// endregion
