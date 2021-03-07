package com.kotlindiscord.kord.extensions.commands.slash

import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.checks.userFor
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.*
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.interaction.InteractionFollowup
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.interaction.FollowupMessageCreateBuilder
import dev.kord.rest.builder.interaction.InteractionApplicationCommandCallbackDataBuilder

/**
 * Command context object representing the context given to message commands.
 *
 * @property interactionResponse Interaction response object, for following up
 */
@OptIn(KordPreview::class)
public open class SlashCommandContext<T : Arguments>(
    private val slashCommand: SlashCommand<out T>,
    event: InteractionCreateEvent,
    commandName: String,
    public var interactionResponse: InteractionResponseBehavior? = null
) : CommandContext(slashCommand, event, commandName, arrayOf()) {
    /** Event that triggered this command execution. **/
    public val event: InteractionCreateEvent get() = eventObj as InteractionCreateEvent

    /** Channel this command happened in. **/
    public open lateinit var channel: Channel

    /** Guild this command happened in. **/
    public open var guild: Guild? = null

    /** Guild member responsible for executing this command. **/
    public open var member: MemberBehavior? = null

    /** User responsible for executing this command. **/
    public open var user: UserBehavior? = null

    /** Arguments object containing this command's parsed arguments. **/
    public open lateinit var arguments: T

    override val command: SlashCommand<out T> get() = slashCommand

    override suspend fun populate() {
        channel = getChannel()
        guild = getGuild()
        member = getMember()
        user = getUser()
    }

    /** @suppress Internal function **/
    public fun populateArgs(args: T) {
        arguments = args
    }

    override suspend fun getChannel(): Channel = channelFor(event)!!.asChannel()
    override suspend fun getGuild(): Guild? = guildFor(event)?.asGuildOrNull()
    override suspend fun getMember(): MemberBehavior? = memberFor(event)?.asMemberOrNull()
    override suspend fun getMessage(): MessageBehavior? = null
    override suspend fun getUser(): UserBehavior? = userFor(event)?.asUserOrNull()

    /**
     * If your command has autoAck set to `false`, use this to acknowledge the command, optionally with a message.
     *
     * Note that Discord only gives you 3 seconds from the interaction create event to acknowledge a command. After
     * that, you won't be able to do anything with it - so it's best to acknowledge it as early as possible.
     */
    public suspend fun ack(
        content: String? = null,
        builder: (InteractionApplicationCommandCallbackDataBuilder.() -> Unit)? = null
    ): InteractionResponseBehavior {
        if (interactionResponse == null) {
            interactionResponse = if (content == null && builder == null) {
                event.interaction.acknowledge()
            } else {
                event.interaction.respond(builder ?: { this.content = content })
            }

            return interactionResponse!!
        }

        error("This command has already been acknowledged.")
    }

    /** Quickly send a reply, mentioning the user. **/
    public suspend fun reply(
        builder: suspend FollowupMessageCreateBuilder.() -> Unit
    ): InteractionFollowup {
        val innerBuilder: suspend FollowupMessageCreateBuilder.() -> Unit = {
            builder()

            content = "${user?.mention?.plus(" ") ?: ""}${content ?: ""}"
        }

        return followUp(innerBuilder)
    }

    /** Quickly send a string reply, mentioning the user. **/
    public suspend fun reply(text: String): InteractionFollowup = reply { content = text }

    /** Quick access to the `followUp` function on the interaction response object. **/
    public suspend fun followUp(builder: suspend FollowupMessageCreateBuilder.() -> Unit): InteractionFollowup {
        if (interactionResponse == null) {
            error("You must acknowledge this command before sending a follow-up message.")
        }

        return interactionResponse!!.followUp { builder() }
    }

    /** Quick access to the `followUp` function on the interaction response object, just for a string message. **/
    public suspend inline fun followUp(content: String): InteractionFollowup = followUp { this.content = content }
}
