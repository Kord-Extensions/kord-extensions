package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.parser.ArgumentParser
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Class representing a message command.
 *
 * You shouldn't need to use this class directly - instead, create an [Extension] and use the
 * [command function][Extension.command] to register your command, by overriding the [Extension.setup]
 * function.
 *
 * @param extension The [Extension] that registered this command.
 */
public open class MessageCommand(extension: Extension) : Command(extension) {
    /**
     * @suppress
     */
    public open lateinit var body: suspend MessageCommandContext.() -> Unit

    /**
     * A description of what this function and how it's intended to be used.
     *
     * This is intended to be made use of by help commands.
     */
    public open var description: String = "No description provided."

    /**
     * Whether this command is enabled and can be invoked.
     *
     * Disabled commands cannot be invoked, and won't be shown in help commands.
     *
     * This can be changed at runtime, if commands need to be enabled and disabled dynamically without being
     * reconstructed.
     */
    public open var enabled: Boolean = true

    /**
     * Whether to hide this command from help command listings.
     *
     * By default, this is `false` - so the command will be shown.
     */
    public open var hidden: Boolean = false

    /**
     * The command signature, specifying how the command's arguments should be structured.
     *
     * You may leave this as it is if your command doesn't take any arguments, you give the [signature] function
     * a dataclass to generate a signature, or you can specify this in the [Extension.command] builder function
     * if you'd like to provide something a bit more specific.
     */
    public open var signature: String = ""

    /**
     * Alternative names that can be used to invoke your command.
     *
     * There's no limit on the number of aliases a command may have, but in the event of an alias matching
     * the [name] of a registered command, the command with the [name] takes priority.
     */
    public open var aliases: Array<String> = arrayOf()

    /**
     * @suppress
     */
    public open val checkList: MutableList<suspend (MessageCreateEvent) -> Boolean> = mutableListOf()

    override val parser: ArgumentParser = ArgumentParser(extension.bot)

    /**
     * An internal function used to ensure that all of a command's required arguments are present.
     *
     * @throws InvalidCommandException Thrown when a required argument hasn't been set.
     */
    @Throws(InvalidCommandException::class)
    public override fun validate() {
        super.validate()

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
    public open fun action(action: suspend MessageCommandContext.() -> Unit) {
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
    public open fun check(vararg checks: suspend (MessageCreateEvent) -> Boolean) {
        checks.forEach { checkList.add(it) }
    }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to this command.
     */
    public open fun check(check: suspend (MessageCreateEvent) -> Boolean) {
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
    public inline fun <reified T : Arguments> signature(noinline builder: () -> T) {
        signature = parser.signature(builder)
    }

    /** Run checks with the provided [MessageCreateEvent]. Return false if any failed, true otherwise. **/
    public open suspend fun runChecks(event: MessageCreateEvent): Boolean {
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
     * @param commandName The name used to invoke this command.
     * @param args Array of command arguments.
     * @param skipChecks Whether to skip testing the command's checks.
     */
    public open suspend fun call(
        event: MessageCreateEvent,
        commandName: String,
        args: Array<String>,
        skipChecks: Boolean = false
    ) {
        if (!skipChecks && !runChecks(event)) {
            return
        }

        val context = MessageCommandContext(this, event, commandName, args)

        context.populate()

        val firstBreadcrumb = if (extension.bot.sentry.enabled) {
            val channel = event.message.getChannelOrNull()
            val guild = event.message.getGuildOrNull()

            val data = mutableMapOf(
                "arguments" to args,
                "message" to event.message.content
            )

            if (channel != null) {
                data["channel"] = when (channel) {
                    is DmChannel -> "Private Message (${channel.id.asString})"
                    is GuildMessageChannel -> "#${channel.name} (${channel.id.asString})"

                    else -> channel.id.asString
                }
            }

            if (guild != null) {
                data["guild"] = "${guild.name} (${guild.id.asString})"
            }

            extension.bot.sentry.createBreadcrumb(
                category = "command",
                type = "user",
                message = "Command \"$name\" called.",
                data = data
            )
        } else {
            null
        }

        @Suppress("TooGenericExceptionCaught")  // Anything could happen here
        try {
            this.body(context)
        } catch (e: ParseException) {
            event.message.respond(e.toString())
        } catch (t: Throwable) {
            if (extension.bot.sentry.enabled) {
                logger.debug { "Submitting error to sentry." }

                lateinit var sentryId: SentryId
                val channel = event.message.getChannelOrNull()

                Sentry.withScope {
                    val author = event.message.author

                    if (author != null) {
                        it.user(author)
                    }

                    it.tag("private", "false")

                    if (channel is DmChannel) {
                        it.tag("private", "true")
                    }

                    it.tag(
                        "command",

                        when (this) {
                            is MessageSubCommand -> this.getFullName()
                            is GroupCommand -> this.getFullName()

                            else -> name
                        }
                    )

                    it.tag("extension", extension.name)

                    it.addBreadcrumb(firstBreadcrumb!!)

                    context.breadcrumbs.forEach { breadcrumb -> it.addBreadcrumb(breadcrumb) }

                    sentryId = Sentry.captureException(t, "MessageCommand execution failed.")

                    logger.debug { "Error submitted to Sentry: $sentryId" }
                }

                extension.bot.sentry.addEventId(sentryId)

                logger.error(t) { "Error during execution of $name command ($event)" }

                if (extension.bot.extensions.containsKey("sentry")) {
                    event.message.respond(
                        "Unfortunately, **an error occurred** during command processing. If you'd like to submit " +
                            "information on what you were doing when this error happened, please use the following " +
                            "command: ```${extension.bot.prefix}feedback $sentryId <message>```"
                    )
                } else {
                    event.message.respond(
                        "Unfortunately, **an error occurred** during command processing. " +
                            "Please let a staff member know."
                    )
                }
            } else {
                logger.error(t) { "Error during execution of $name command ($event)" }

                event.message.respond(
                    "Unfortunately, **an error occurred** during command processing. " +
                        "Please let a staff member know."
                )
            }
        }
    }
}
