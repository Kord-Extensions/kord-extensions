package com.kotlindiscord.kord.extensions.commands.slash

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import mu.KLogger
import mu.KotlinLogging

private val logger: KLogger = KotlinLogging.logger {}
private const val DISCORD_LIMIT: Int = 10

/**
 * Object representing a set of grouped slash commands.
 *
 * @param name Name of this command group, shown on Discord.
 * @param parent Root/top-level command that owns this group.
 */
public open class SlashGroup(
    public val name: String,
    public val parent: SlashCommand<out Arguments>
) {
    /** List of subcommands belonging to this group. **/
    public val subCommands: MutableList<SlashCommand<out Arguments>> = mutableListOf()

    /** Command group description, which is required and shown on Discord. **/
    public lateinit var description: String

    /**
     * Validate this command group, ensuring it has everything it needs.
     *
     * Throws if not.
     */
    public open fun validate() {
        if (!::description.isInitialized) {
            throw InvalidCommandException(name, "No group description given.")
        }

        if (subCommands.isEmpty()) {
            error("Command groups must contain at least one subcommand.")
        }
    }

    /**
     * DSL function for easily registering a grouped subcommand, with arguments.
     *
     * Use this in your group function to register a grouped subcommand that may be executed on Discord.
     *
     * @param arguments Arguments builder (probably a reference to the class constructor).
     * @param body Builder lambda used for setting up the subcommand object.
     */
    public open suspend fun <T : Arguments> subCommand(
        arguments: (() -> T)?,
        body: suspend SlashCommand<T>.() -> Unit
    ): SlashCommand<T> {
        val commandObj = SlashCommand(parent.extension, arguments, parent, this)
        body.invoke(commandObj)

        return subCommand(commandObj)
    }

    /**
     * DSL function for easily registering a grouped subcommand, without arguments.
     *
     * Use this in your group function to register a grouped subcommand that may be executed on Discord.
     *
     * @param body Builder lambda used for setting up the subcommand object.
     */
    public open suspend fun subCommand(
        body: suspend SlashCommand<out Arguments>.() -> Unit
    ): SlashCommand<out Arguments> {
        val commandObj = SlashCommand<Arguments>(parent.extension, null, parent, this)
        body.invoke(commandObj)

        return subCommand(commandObj)
    }

    /**
     * Function for registering a grouped custom slash command object, for subcommands.
     *
     * You can use this if you have a custom slash command subclass you need to register.
     *
     * @param commandObj SlashCommand object to register as a grouped subcommand.
     */
    public open suspend fun <T : Arguments> subCommand(
        commandObj: SlashCommand<T>
    ): SlashCommand<T> {
        if (subCommands.size >= DISCORD_LIMIT) {
            error("Groups may only contain up to 10 subcommands.")
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
}
