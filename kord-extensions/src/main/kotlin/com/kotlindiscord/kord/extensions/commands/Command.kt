@file:Suppress("UnnecessaryAbstractClass")  // No idea why we're getting this

package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.commands.events.CommandEvent
import com.kotlindiscord.kord.extensions.extensions.Extension
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Abstract base class representing the few things that command objects can have in common.
 *
 * This should be used as a base class only for command types that aren't related to the other command types.
 *
 * @property extension The extension object this command belongs to.
 */
@ExtensionDSL
public abstract class Command(public val extension: Extension) {
    /**
     * The name of this command, for invocation and help commands.
     */
    public open lateinit var name: String

    /**
     * An internal function used to ensure that all of a command's required arguments are present and correct.
     *
     * @throws InvalidCommandException Thrown when a required argument hasn't been set or is invalid.
     */
    @Throws(InvalidCommandException::class)
    public open fun validate() {
        if (!::name.isInitialized || name.isEmpty()) {
            throw InvalidCommandException(null, "No command name given.")
        }
    }

    /** Quick shortcut for emitting a command event without blocking. **/
    public open suspend fun emitEventAsync(event: CommandEvent<*, *>): Job =
        event.launch {
            extension.bot.send(event)
        }
}
