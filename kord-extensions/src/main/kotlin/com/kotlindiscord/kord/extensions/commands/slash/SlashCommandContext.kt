package com.kotlindiscord.kord.extensions.commands.slash

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.*
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.interaction.FollowupMessage
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.interaction.FollowupMessageCreateBuilder

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
    public val interactionResponse: InteractionResponseBehavior
) : CommandContext(slashCommand, event, commandName, arrayOf()) {
    /** Event that triggered this command execution. **/
    public val event: InteractionCreateEvent get() = eventObj as InteractionCreateEvent

    /** Channel this command happened in. **/
    public open lateinit var channel: Channel

    /** Guild this command happened in. **/
    public open lateinit var guild: Guild

    /** Guild member responsible for executing this command. **/
    public open lateinit var member: MemberBehavior

    /** User responsible for executing this command. **/
    public open lateinit var user: UserBehavior

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

    override suspend fun getChannel(): Channel = event.interaction.getChannel()
    override suspend fun getGuild(): Guild = event.interaction.getGuild()
    override suspend fun getMember(): MemberBehavior = event.interaction.member
    override suspend fun getMessage(): MessageBehavior? = null
    override suspend fun getUser(): UserBehavior = event.interaction.member

    /** Quick access to the `followUp` function on the interaction response object. **/
    public suspend inline fun followUp(builder: FollowupMessageCreateBuilder.() -> Unit): FollowupMessage =
        interactionResponse.followUp(builder)

    /** Quick access to the `followUp` function on the interaction response object, just for a string message. **/
    public suspend inline fun followUp(content: String): FollowupMessage = followUp { this.content = content }
}
