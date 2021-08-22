package com.kotlindiscord.kord.extensions.extensions

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.commands.content.MessageContentCommand
import com.kotlindiscord.kord.extensions.commands.content.MessageContentGroupCommand
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommand
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * DSL function for easily registering a command.
 *
 * Use this in your setup function to register a command that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the command object.
 */
@ExtensionDSL
public suspend fun <T : Arguments> Extension.messageContentCommand(
    arguments: () -> T,
    body: suspend MessageContentCommand<T>.() -> Unit
): MessageContentCommand<T> {
    val commandObj = MessageContentCommand(this, arguments)
    body.invoke(commandObj)

    return messageContentCommand(commandObj)
}

/**
 * DSL function for easily registering a command, without arguments.
 *
 * Use this in your setup function to register a command that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the command object.
 */
@ExtensionDSL
public suspend fun Extension.messageContentCommand(
    body: suspend MessageContentCommand<Arguments>.() -> Unit
): MessageContentCommand<Arguments> {
    val commandObj = MessageContentCommand<Arguments>(this)
    body.invoke(commandObj)

    return messageContentCommand(commandObj)
}

/**
 * Function for registering a custom command object.
 *
 * You can use this if you have a custom command subclass you need to register.
 *
 * @param commandObj MessageContentCommand object to register.
 */
public fun <T : Arguments> Extension.messageContentCommand(
    commandObj: MessageContentCommand<T>
): MessageContentCommand<T> {
    try {
        commandObj.validate()
        messageContentCommandsRegistry.add(commandObj)
        commands.add(commandObj)
    } catch (e: CommandRegistrationException) {
        logger.error(e) { "Failed to register command - $e" }
    } catch (e: InvalidCommandException) {
        logger.error(e) { "Failed to register command - $e" }
    }

    return commandObj
}

/**
 * DSL function for easily registering a slash command, with arguments.
 *
 * Use this in your setup function to register a slash command that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <T : Arguments> Extension.slashCommand(
    arguments: () -> T,
    body: suspend SlashCommand<T>.() -> Unit
): SlashCommand<T> {
    val commandObj = SlashCommand(this, arguments)
    body.invoke(commandObj)

    return slashCommand(commandObj)
}

/**
 * DSL function for easily registering a slash command, without arguments.
 *
 * Use this in your setup function to register a slash command that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun Extension.slashCommand(
    body: suspend SlashCommand<out Arguments>.() -> Unit
): SlashCommand<out Arguments> {
    val commandObj = SlashCommand<Arguments>(this, null)
    body.invoke(commandObj)

    return slashCommand(commandObj)
}

/**
 * Function for registering a custom slash command object.
 *
 * You can use this if you have a custom slash command subclass you need to register.
 *
 * @param commandObj SlashCommand object to register.
 */
public fun <T : Arguments> Extension.slashCommand(
    commandObj: SlashCommand<T>
): SlashCommand<T> {
    try {
        commandObj.validate()
        slashCommands.add(commandObj)
        slashCommandsRegistry.register(commandObj, commandObj.guild)
    } catch (e: CommandRegistrationException) {
        logger.error(e) { "Failed to register slash command - $e" }
    } catch (e: InvalidCommandException) {
        logger.error(e) { "Failed to register slash command - $e" }
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
public suspend fun <T : Arguments> Extension.messageContentGroupCommand(
    arguments: () -> T,
    body: suspend MessageContentGroupCommand<T>.() -> Unit
): MessageContentGroupCommand<T> {
    val commandObj = MessageContentGroupCommand(this, arguments)
    body.invoke(commandObj)

    return messageContentCommand(commandObj) as MessageContentGroupCommand
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
public suspend fun Extension.messageContentGroupCommand(
    body: suspend MessageContentGroupCommand<Arguments>.() -> Unit
): MessageContentGroupCommand<Arguments> {
    val commandObj = MessageContentGroupCommand<Arguments>(this)
    body.invoke(commandObj)

    return messageContentCommand(commandObj) as MessageContentGroupCommand
}
