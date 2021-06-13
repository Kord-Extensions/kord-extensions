package com.kotlindiscord.kord.extensions.commands.slash

import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.ComponentInteractionContext
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.InteractionResponseBehavior
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.entity.interaction.InteractionFollowup
import dev.kord.core.entity.interaction.PublicFollowupMessage
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.component.ButtonBuilder
import dev.kord.rest.builder.interaction.EphemeralFollowupMessageCreateBuilder
import dev.kord.rest.builder.interaction.PublicFollowupMessageCreateBuilder

/**
 * Command context object representing the context given to message commands.
 *
 * @property interactionResponse Interaction response object, for following up
 */
@OptIn(KordPreview::class)
@ExtensionDSL
public open class SlashCommandContext<T : Arguments>(
    private val slashCommand: SlashCommand<out T>,
    event: InteractionCreateEvent,
    commandName: String,
    public var interactionResponse: InteractionResponseBehavior? = null
) : CommandContext(slashCommand, event, commandName, arrayOf()) {
    /** Event that triggered this command execution. **/
    public val event: InteractionCreateEvent get() = eventObj as InteractionCreateEvent

    /** Quick access to the [CommandInteraction]. **/
    public val interaction: CommandInteraction get() = event.interaction as CommandInteraction

    /** Channel this command happened in. **/
    public open lateinit var channel: MessageChannel

    /** Guild this command happened in. **/
    public open var guild: Guild? = null

    /** Guild member responsible for executing this command. **/
    public open var member: MemberBehavior? = null

    /** User responsible for executing this command. **/
    public open lateinit var user: UserBehavior

    /** Arguments object containing this command's parsed arguments. **/
    public open lateinit var arguments: T

    /** Whether a response or ack has already been sent by the user. **/
    public open val acked: Boolean get() = interactionResponse != null

    /** Whether we're working ephemerally, or null if no ack or response was sent yet. **/
    public open val isEphemeral: Boolean?
        get() = when (interactionResponse) {
            is EphemeralInteractionResponseBehavior -> true
            is PublicInteractionResponseBehavior -> false

            else -> null
        }

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

    override suspend fun getChannel(): MessageChannel = channelFor(event)!!.asChannel() as MessageChannel
    override suspend fun getGuild(): Guild? = guildFor(event)?.asGuildOrNull()
    override suspend fun getMember(): MemberBehavior? = memberFor(event)?.asMemberOrNull()
    override suspend fun getMessage(): MessageBehavior? = null
    override suspend fun getUser(): UserBehavior = event.interaction.user

    /**
     * Send an acknowledgement manually, assuming you have `autoAck` set to `NONE`.
     *
     * Note that what you supply for `ephemeral` will decide how the rest of your interactions - both responses and
     * follow-ups. They must match in ephemeral state.
     *
     * This function will throw an exception if an acknowledgement or response has already been sent.
     *
     * @param ephemeral Whether this should be an ephemeral acknowledgement or not.
     */
    public suspend fun ack(ephemeral: Boolean): InteractionResponseBehavior {
        if (acked) {
            error("Attempted to acknowledge an interaction that's already been acknowledged.")
        }

        interactionResponse = if (ephemeral) {
            event.interaction.acknowledgeEphemeral()
        } else {
            event.interaction.acknowledgePublic()
        }

        return interactionResponse!!
    }

    /**
     * Assuming an acknowledgement or response has been sent, send an ephemeral follow-up message.
     *
     * This function will throw an exception if no acknowledgement or response has been sent yet, or this interaction
     * has already been interacted with in a non-ephemeral manner.
     *
     * Note that ephemeral follow-ups require a content string, and may not contain embeds or files.
     */
    public suspend inline fun ephemeralFollowUp(
        content: String,
        builder: EphemeralFollowupMessageCreateBuilder.() -> Unit = {}
    ): InteractionFollowup {
        if (interactionResponse == null) {
            error("Tried send an interaction follow-up before acknowledging it.")
        }

        if (isEphemeral == false) {
            error("Tried send an ephemeral follow-up for a public interaction.")
        }

        return (interactionResponse as EphemeralInteractionResponseBehavior).followUp(content, builder)
    }

    /**
     * Assuming an acknowledgement or response has been sent, send a public follow-up message.
     *
     * This function will throw an exception if no acknowledgement or response has been sent yet, or this interaction
     * has already been interacted with in an ephemeral manner.
     */
    public suspend inline fun publicFollowUp(
        builder: PublicFollowupMessageCreateBuilder.() -> Unit
    ): PublicFollowupMessage {
        if (interactionResponse == null) {
            error("Tried send an interaction follow-up before acknowledging it.")
        }

        if (isEphemeral == true) {
            error("Tried to send a public follow-up for an ephemeral interaction.")
        }

        return (interactionResponse as PublicInteractionResponseBehavior).followUp(builder)
    }

    /**
     * Register an event handler for this specific button, to be fired when it's clicked.
     *
     * Will use the same ack type that this slash command's [isEphemeral] reports. If you'd like to use a different
     * one, you can use the variant of this function that's present in [command.extension].
     *
     * **Note:** Buttons will not be automatically disabled after [timeout]/[fireOnce] has been met.
     *
     * @param timeout How long to wait before the bot stops waiting for button clicks, if needed.
     * @param fireOnce Whether to stop listening for clicks after the first one.
     * @param body Code to run after the click.
     */
    @OptIn(KordPreview::class)
    @ExtensionDSL
    public suspend fun ButtonBuilder.InteractionButtonBuilder.action(
        timeout: Long? = null,
        fireOnce: Boolean = true,
        body: suspend ComponentInteractionContext.() -> Unit
    ): Unit = with(command.extension) {
        action(
            ackType = when (isEphemeral) {
                true -> AutoAckType.EPHEMERAL
                false -> AutoAckType.PUBLIC

                else -> null
            },

            timeout = timeout,
            fireOnce = fireOnce,
            body = body
        )
    }
}
