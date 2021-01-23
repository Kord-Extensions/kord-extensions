package com.kotlindiscord.kord.extensions.commands.slash

import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.parser.SlashCommandParser
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.interaction.InteractionCreateEvent
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import mu.KLogger
import mu.KotlinLogging

private val logger: KLogger = KotlinLogging.logger {}

/**
 * Class representing a slash command.
 *
 * You shouldn't need to use this class directly - instead, create an [Extension] and use the
 * [slash command function][Extension.slashCommand] to register your command, by overriding the [Extension.setup]
 * function.
 *
 * @param extension The [Extension] that registered this command.
 * @param arguments Arguments object builder for this command, if it has arguments.
 */
public open class SlashCommand<T : Arguments>(
    extension: Extension,
    public open val arguments: (() -> T)? = null
) : Command(extension) {
    /** Command description, as displayed on Discord. **/
    public open lateinit var description: String

    /** @suppress **/
    public open lateinit var body: suspend SlashCommandContext<out T>.() -> Unit

    /** Guild ID this slash command is to be registered for, if any. **/
    public open var guild: Snowflake? = null

    /** Whether to automatically acknowledge this command. Make sure you `ack` your command within 3 seconds! **/
    public open var autoAck: Boolean = true

    /** Whether to send a message on discord showing the command invocation. **/
    public open var showSource: Boolean = false

    /** @suppress **/
    public open val checkList: MutableList<suspend (InteractionCreateEvent) -> Boolean> = mutableListOf()

    public override val parser: SlashCommandParser = SlashCommandParser(extension.bot)

    /**
     * An internal function used to ensure that all of a command's required properties are present.
     *
     * @throws InvalidCommandException Thrown when a required property hasn't been set.
     */
    @Throws(InvalidCommandException::class)
    public override fun validate() {
        super.validate()

        if (!::description.isInitialized) {
            throw InvalidCommandException(name, "No command description given.")
        }

        if (!::body.isInitialized) {
            throw InvalidCommandException(name, "No command action given.")
        }
    }

    // region: DSL functions

    /** Specify a specific guild for this slash command. **/
    public open fun guild(guild: Snowflake) {
        this.guild = guild
    }

    /** Specify a specific guild for this slash command. **/
    public open fun guild(guild: Long) {
        this.guild = Snowflake(guild)
    }

    /** Specify a specific guild for this slash command. **/
    public open fun guild(guild: GuildBehavior) {
        this.guild = guild.id
    }

    /**
     * Define what will happen when your command is invoked.
     *
     * @param action The body of your command, which will be executed when your command is invoked.
     */
    public open fun action(action: suspend SlashCommandContext<out T>.() -> Unit) {
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
    public open fun check(vararg checks: suspend (InteractionCreateEvent) -> Boolean) {
        checks.forEach { checkList.add(it) }
    }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to this command.
     */
    public open fun check(check: suspend (InteractionCreateEvent) -> Boolean) {
        checkList.add(check)
    }

    // endregion

    /** Run checks with the provided [InteractionCreateEvent]. Return false if any failed, true otherwise. **/
    public open suspend fun runChecks(event: InteractionCreateEvent): Boolean {
        for (check in checkList) {
            if (!check.invoke(event)) {
                return false
            }
        }
        return true
    }

    /**
     * Execute this command, given an [InteractionCreateEvent].
     *
     * This function takes a [InteractionCreateEvent] (generated when a slash command is executed), and
     * processes it. The command's checks are invoked and, assuming all of the
     * checks passed, the [command body][action] is executed.
     *
     * If an exception is thrown by the [command body][action], it is caught and a traceback
     * is printed.
     *
     * @param event The interaction creation event.
     */
    public open suspend fun call(event: InteractionCreateEvent) {
        val sentry = extension.bot.sentry

        if (!this.runChecks(event)) {
            return
        }

        val resp = if (autoAck) {
            event.interaction.acknowledge(this.showSource)
        } else {
            null
        }

        val context = SlashCommandContext(this, event, this.name, resp)

        context.populate()

        val firstBreadcrumb = if (sentry.enabled) {
            val channel = context.channel.asChannelOrNull()
            val guild = context.guild.asGuildOrNull()

            val data = mutableMapOf(
                "command" to this.name
            )

            if (this.guild != null) {
                data["command.guild"] to this.guild!!.asString
            }

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

            sentry.createBreadcrumb(
                category = "command.slash",
                type = "user",
                message = "Slash command \"${this.name}\" called.",
                data = data
            )
        } else {
            null
        }

        @Suppress("TooGenericExceptionCaught")
        try {
            if (this.arguments != null) {
                val args = this.parser.parse(this.arguments!!, context)
                context.populateArgs(args)
            }

            this.body.invoke(context)
        } catch (e: ParseException) {
            if (resp != null) {
                context.reply(e.reason)
            } else {
                context.ack(showSource, e.reason)
            }
        } catch (t: Throwable) {
            if (sentry.enabled) {
                logger.debug { "Submitting error to sentry." }

                lateinit var sentryId: SentryId
                val channel = context.channel
                val author = context.user.asUserOrNull()

                Sentry.withScope {
                    if (author != null) {
                        it.user(author)
                    }

                    it.tag("private", "false")

                    if (channel is DmChannel) {
                        it.tag("private", "true")
                    }

                    it.tag("command", this.name)
                    it.tag("extension", this.extension.name)

                    it.addBreadcrumb(firstBreadcrumb!!)

                    context.breadcrumbs.forEach { breadcrumb -> it.addBreadcrumb(breadcrumb) }

                    sentryId = Sentry.captureException(t, "MessageCommand execution failed.")

                    logger.debug { "Error submitted to Sentry: $sentryId" }
                }

                sentry.addEventId(sentryId)

                logger.error(t) { "Error during execution of ${this.name} slash command ($event)" }

                val errorMessage = if (extension.bot.extensions.containsKey("sentry")) {
                    "Unfortunately, **an error occurred** during command processing. If you'd " +
                        "like to submit information on what you were doing when this error happened, " +
                        "please use the following command: " +
                        "```${extension.bot.prefix}feedback $sentryId <message>```"
                } else {
                    "Unfortunately, **an error occurred** during command processing. " +
                        "Please let a staff member know."
                }

                if (resp != null) {
                    context.reply(errorMessage)
                } else {
                    context.ack(showSource, errorMessage)
                }
            } else {
                logger.error(t) { "Error during execution of ${this.name} slash command ($event)" }

                val errorMessage = "Unfortunately, **an error occurred** during command processing. " +
                    "Please let a staff member know."

                if (resp != null) {
                    context.reply(errorMessage)
                } else {
                    context.ack(showSource, errorMessage)
                }
            }
        }
    }
}
