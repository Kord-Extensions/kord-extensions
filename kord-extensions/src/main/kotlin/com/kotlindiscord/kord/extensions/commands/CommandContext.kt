package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.event.Event
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
 * @param commandName MessageCommand name given by the user to invoke the command - lower-cased.
 * @param args Array of string arguments for this command.
 */
public abstract class CommandContext(
    public open val command: MessageCommand,
    public open val eventObj: Event,
    public open val commandName: String,
    public open val args: Array<String>
) {
    /** A list of Sentry breadcrumbs created during command execution. **/
    public open val breadcrumbs: MutableList<Breadcrumb> = mutableListOf()

    /** Called before command processing, used to populate any extra variables from event data. **/
    public abstract suspend fun populate()

    /** Extract channel information from event data, if that context is available. **/
    public abstract suspend fun getChannel(): ChannelBehavior?

    /** Extract guild information from event data, if that context is available. **/
    public abstract suspend fun getGuild(): GuildBehavior?

    /** Extract member information from event data, if that context is available. **/
    public abstract suspend fun getMember(): MemberBehavior?

    /** Extract message information from event data, if that context is available. **/
    public abstract suspend fun getMessage(): MessageBehavior?

    /** Extract user information from event data, if that context is available. **/
    public abstract suspend fun getUser(): UserBehavior?

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
    ): Breadcrumb {
        val crumb = command.extension.bot.sentry.createBreadcrumb(category, level, message, type, data)

        breadcrumbs.add(crumb)

        return crumb
    }
}
