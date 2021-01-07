package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.commands.parser.ArgumentParser
import com.kotlindiscord.kord.extensions.extensions.Extension

public abstract class Command(public val extension: Extension) {
    /**
     * The name of this command, for invocation and help commands.
     */
    public open lateinit var name: String

    /**
     * @suppress
     */
    public abstract val parser: ArgumentParser

    /**
     * An internal function used to ensure that all of a command's required arguments are present.
     *
     * @throws InvalidCommandException Thrown when a required argument hasn't been set.
     */
    @Throws(InvalidCommandException::class)
    public open fun validate() {
        if (!::name.isInitialized) {
            throw InvalidCommandException(null, "No command name given.")
        }
    }
}
