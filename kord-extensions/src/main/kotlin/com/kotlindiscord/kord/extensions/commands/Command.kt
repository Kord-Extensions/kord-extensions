package com.kotlindiscord.kord.extensions.commands

import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.parser.ArgumentParser
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import mu.KotlinLogging
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

private val logger = KotlinLogging.logger {}

/**
 * @suppress
 */
val listType = List::class.createType(arguments = listOf(KTypeProjection.STAR))

/**
 * Class representing a single command.
 *
 * You shouldn't need to use this class directly - instead, create an [Extension] and use the
 * [command function][Extension.command] to register your command, by overriding the [Extension.setup]
 * function.
 *
 * @param extension The [Extension] that registered this command.
 */
open class Command(val extension: Extension) {
    /**
     * @suppress
     */
    open lateinit var body: suspend CommandContext.() -> Unit

    /**
     * The name of this command, for invocation and help commands.
     */
    open lateinit var name: String

    /**
     * A description of what this function and how it's intended to be used.
     *
     * This is intended to be made use of by help commands.
     */
    open var description: String = "No description provided."

    /**
     * Whether this command is enabled and can be invoked.
     *
     * Disabled commands cannot be invoked, and won't be shown in help commands.
     *
     * This can be changed at runtime, if commands need to be enabled and disabled dynamically without being
     * reconstructed.
     */
    open var enabled: Boolean = true

    /**
     * Whether to hide this command from help command listings.
     *
     * By default, this is `false` - so the command will be shown.
     */
    open var hidden: Boolean = false

    /**
     * The command signature, specifying how the command's arguments should be structured.
     *
     * You may leave this as it is if your command doesn't take any arguments, you give the [signature] function
     * a dataclass to generate a signature, or you can specify this in the [Extension.command] builder function
     * if you'd like to provide something a bit more specific.
     */
    open var signature: String = ""

    /**
     * Alternative names that can be used to invoke your command.
     *
     * There's no limit on the number of aliases a command may have, but in the event of an alias matching
     * the [name] of a registered command, the command with the [name] takes priority.
     */
    open var aliases: Array<String> = arrayOf()

    /**
     * @suppress
     */
    open val checkList: MutableList<suspend (MessageCreateEvent) -> Boolean> = mutableListOf()

    /**
     * @suppress
     */
    open val parser = ArgumentParser(extension.bot)

    /**
     * An internal function used to ensure that all of a command's required arguments are present.
     *
     * @throws InvalidCommandException Thrown when a required argument hasn't been set.
     */
    @Throws(InvalidCommandException::class)
    open fun validate() {
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
    open fun action(action: suspend CommandContext.() -> Unit) {
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
    open fun check(vararg checks: suspend (MessageCreateEvent) -> Boolean) {
        checks.forEach { checkList.add(it) }
    }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to this command.
     */
    open fun check(check: suspend (MessageCreateEvent) -> Boolean) {
        checkList.add(check)
    }

    // endregion

    /**
     * Attempt to generate a signature string from a given data class.
     *
     * This will produce \[argument] for optional parameters, and <argument> for required parameters. List
     * parameters will produce \[argument ...] or <argument ...> respectively.
     *
     * @param T Data class to generate a signature string for.
     * @throws ParseException Thrown if the class passed isn't a data class.
     */
    @Throws(ParseException::class)
    inline fun <reified T : Arguments> signature(noinline builder: () -> T) {
        signature = parser.signature(builder)
    }

    /** Run checks with the provided [MessageCreateEvent]. Return false if any failed, true otherwise. **/
    open suspend fun runChecks(event: MessageCreateEvent): Boolean {
        for (check in checkList) {
            if (!check.invoke(event)) {
                return false
            }
        }
        return true
    }

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
    open suspend fun call(event: MessageCreateEvent, args: Array<String>, skipChecks: Boolean = false) {
        if (!skipChecks && !runChecks(event)) {
            return
        }

        @Suppress("TooGenericExceptionCaught")  // Anything could happen here
        try {
            this.body(CommandContext(this, event, args))
        } catch (e: ParseException) {
            event.message.respond(e.toString())
        } catch (e: Exception) {
            logger.error(e) { "Error during execution of $name command ($event)" }
        }
    }
}
