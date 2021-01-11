package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.extensions.Extension

/**
 * Class representing a subcommand.
 *
 * This is used for group commands, so that subcommands are aware of their parent.
 *
 * @param extension The [Extension] that registered this command.
 * @param parent The [GroupCommand] this command exists under.
 */
public open class MessageSubCommand(extension: Extension, public open val parent: GroupCommand) :
    MessageCommand(extension) {
    /**
     * Get the name of this command, prefixed with the name of its parent (separated by spaces).
     */
    public open fun getFullName(): String = parent.getFullName() + " " + this.name
}
