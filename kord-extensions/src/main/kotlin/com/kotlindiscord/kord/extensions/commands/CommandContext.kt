package com.kotlindiscord.kord.extensions.commands

import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import io.sentry.Breadcrumb
import io.sentry.SentryLevel

/**
 * Light wrapper class representing the context for a command's action.
 *
 * This is what `this` refers to in a command action body. You shouldn't have to
 * instantiate this yourself.
 *
 * @param command Respective command for this context object.
 * @param event Event that triggered this command.
 * @param commandName Command name given by the user to invoke the command - lower-cased.
 * @param args Array of string arguments for this command.
 */
public open class CommandContext(
    public open val command: Command,
    public open val event: MessageCreateEvent,
    public open val commandName: String,
    public open val args: Array<String>
) {
    /**
     * Message object representing the message that invoked the command.
     */
    public open val message: Message by lazy { event.message }

    /** A list of Sentry breadcrumbs created during command execution. **/
    public open val breadcrumbs: MutableList<Breadcrumb> = mutableListOf()

    /**
     * Attempt to parse the arguments in this CommandContext into a given data class.
     *
     * @param T Data class to parse arguments into.
     * @throws ParseException Thrown when parsing fails. If you don't catch this, an error message will be sent.
     */
    @Throws(ParseException::class)
    public suspend inline fun <reified T : Arguments> parse(noinline builder: () -> T): T =
        command.parser.parse(builder, this)

    /**
     * Add a Sentry breadcrumb to this command context.
     *
     * This should be used for the purposes of tracing what exactly is happening during your
     * command processing. If the bot administrator decides to enable Sentry integration, the
     * breadcrumbs will be sent to Sentry when there's a command processing error.
     */
    public fun breadcrumb(
        category: String? = null,
        level: SentryLevel? = null,
        message: String? = null,
        type: String? = null,

        data: Map<String, Any> = mapOf()
    ): Breadcrumb = command.extension.bot.sentry.createBreadcrumb(category, level, message, type, data)
}
