package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.InvalidCommandException
import mu.KLogger
import mu.KotlinLogging

/**
 * Slash command group, containing other slash commands.
 *
 * @param name Slash command group name
 * @param parent Parent slash command that this group belongs to
 */
public class SlashGroup(
    public val name: String,
    public val parent: SlashCommand<*, *>
) {
    internal val logger: KLogger = KotlinLogging.logger {}

    /** List of subcommands belonging to this group. **/
    public val subCommands: MutableList<SlashCommand<*, *>> = mutableListOf()

    /** Command group description, which is required and shown on Discord. **/
    public lateinit var description: String

    /**
     * Validate this command group, ensuring it has everything it needs.
     *
     * Throws if not.
     */
    public fun validate() {
        if (!::description.isInitialized) {
            throw InvalidCommandException(name, "No group description given.")
        }

        if (subCommands.isEmpty()) {
            error("Command groups must contain at least one subcommand.")
        }
    }
}
