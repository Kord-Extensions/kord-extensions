package com.kotlindiscord.kord.extensions.commands

import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.parser.Arguments

/**
 * Light wrapper class representing the context for a command's action.
 *
 * This is what `this` refers to in a command action body. You shouldn't have to
 * instantiate this yourself.
 *
 * @param command Respective command for this context object.
 * @param event Event that triggered this command.
 * @param args Array of string arguments for this command.
 */
public open class CommandContext(
    public open val command: Command,
    public open val event: MessageCreateEvent,
    public open val args: Array<String>
) {
    /**
     * Message object representing the message that invoked the command.
     */
    public open val message: Message by lazy { event.message }

    /**
     * Attempt to parse the arguments in this CommandContext into a given data class.
     *
     * @param T Data class to parse arguments into.
     * @throws ParseException Thrown when parsing fails. If you don't catch this, an error message will be sent.
     */
    @Throws(ParseException::class)
    public suspend inline fun <reified T : Arguments> parse(noinline builder: () -> T): T =
        command.parser.parse(builder, this)
}
