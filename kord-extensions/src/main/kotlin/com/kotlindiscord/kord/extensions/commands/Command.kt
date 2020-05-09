package com.kotlindiscord.kord.extensions.commands

import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.extensions.Extension
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Class representing a single command.
 *
 * You shouldn't need to use this class directly - instead, create an [Extension] and use the
 * [command function][Extension.command] to register your command, by overriding the [Extension.setup]
 * function.
 *
 * @param extension The [Extension] that registered this command.
 */
class Command(val extension: Extension) {
    /**
     * @suppress
     */
    lateinit var body: suspend CommandContext.() -> Unit

    /**
     * The name of this command, for invocation and help commands.
     */
    lateinit var name: String

    /**
     * A description of what this function and how it's intended to be used.
     *
     * This is intended to be made use of by help commands.
     */
    var description: String = "No description provided."

    /**
     * Whether this command is enabled and can be invoked.
     *
     * Disabled commands cannot be invoked, and won't be shown in help commands.
     *
     * This can be changed at runtime, if commands need to be enabled and disabled dynamically without being
     * reconstructed.
     */
    var enabled: Boolean = true

    /**
     * Whether to hide this command from help command listings.
     *
     * By default, this is `false` - so the command will be shown.
     */
    var hidden: Boolean = false  // TODO: Help commands should also execute checks

    /**
     * The command signature, specifying how the command's arguments should be structured.
     *
     * You may leave this blank if your command doesn't have any arguments. Otherwise, specify
     * your arguments in the following manner: `"<required> '<required with multiple words>' \[optional]"`
     */
    var signature: String = ""  // TODO: Proper arg parsing

    /**
     * Alternative names that can be used to invoke your command.
     *
     * There's no limit on the number of aliases a command may have, but in the event of an alias matching
     * the [name] of a registered command, the command with the [name] takes priority.
     */
    var aliases: Array<String> = arrayOf()

    /**
     * @suppress
     */
    val checkList: MutableList<suspend (MessageCreateEvent) -> Boolean> = mutableListOf()

    /**
     * @suppress
     */
    val parser = ArgumentParser(extension.bot)

    /**
     * An internal function used to ensure that all of a command's required arguments are present.
     *
     * @throws InvalidCommandException Thrown when a required argument hasn't been set.
     */
    @Throws(InvalidCommandException::class)
    fun validate() {
        if (!::name.isInitialized) {
            throw InvalidCommandException(null, "No command name given.")
        }

        if (!::body.isInitialized) {
            throw InvalidCommandException(name, "No command action given.")
        }
    }

    // region: DSL functions

    /**
     * Define what will happen when your command is invoked.
     *
     * @param action The body of your command, which will be executed when your command is invoked.
     */
    fun action(action: suspend CommandContext.() -> Unit) {
        // TODO: Documented @samples
        this.body = action
    }

    /**
     * Define a check which must pass for the command to be executed.
     *
     * A command may have multiple checks - all checks must pass for the command to be executed.
     * Checks will be run in the order that they're defined.
     *
     * This function can be used DSL-style with a given body, or it can be passed one or more
     * predefined functions. See the samples for more information.
     *
     * @param checks Checks to apply to this command.
     */
    fun check(vararg checks: suspend (MessageCreateEvent) -> Boolean) {
        // TODO: Documented @samples
        checks.forEach { checkList.add(it) }
    }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to this command.
     */
    fun check(check: suspend (MessageCreateEvent) -> Boolean) {
        // TODO: Documented @samples
        checkList.add(check)
    }

    // endregion

    /**
     * Execute this command, given a [MessageCreateEvent].
     *
     * This function takes a [MessageCreateEvent] (generated when a message is received), and
     * processes it. The command's checks are invoked and, assuming all of the
     * checks passed, the [command body][action] is executed.
     *
     * If an exception is thrown by the [command body][action], it is caught and a traceback
     * is printed.
     *
     * @param event The message creation event.
     */
    suspend fun call(event: MessageCreateEvent, args: Array<String>) {
        for (check in checkList) {
            if (!check.invoke(event)) {
                return
            }
        }

        @Suppress("TooGenericExceptionCaught")  // Anything could happen here
        try {
            this.body(CommandContext(this, event, args))
        } catch (e: ParseException) {
            event.message.channel.createMessage(e.toString())
        } catch (e: Exception) {
            logger.error(e) { "Error during execution of $name command ($event)" }
        }
    }
}
