package com.kotlindiscord.kord.extensions.commands.application

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.interaction.ApplicationInteractionCreateEvent
import org.koin.core.component.inject

/**
 * Base class representing the shared functionality for an application command's context.
 *
 * @param genericEvent Generic event object to populate data from.
 * @param genericCommand Generic command object that this context belongs to.
 */
public abstract class ApplicationCommandContext(
    public val genericEvent: ApplicationInteractionCreateEvent,
    public val genericCommand: ApplicationCommand<*>
) : CommandContext(genericCommand, genericEvent, genericCommand.name) {
    /** Current bot setting object. **/
    public val botSettings: ExtensibleBotBuilder by inject()

    /** Channel this command was executed within. **/
    public open lateinit var channel: MessageChannelBehavior

    /** Guild this command was executed within, if any. **/
    public open var guild: GuildBehavior? = null

    /** Member that executed this command, if on a guild. **/
    public open var member: MemberBehavior? = null

    /** User that executed this command. **/
    public open lateinit var user: UserBehavior

    /** Called before processing, used to populate any extra variables from event data. **/
    public override suspend fun populate() {
        // NOTE: This must always be alphabetical, some latter calls rely on earlier ones

        channel = getChannel()
        guild = getGuild()
        member = getMember()
        user = getUser()
    }

    /** Extract channel information from event data, if that context is available. **/
    public override suspend fun getChannel(): MessageChannelBehavior =
        genericEvent.interaction.getChannel()

    /** Extract guild information from event data, if that context is available. **/
    public override suspend fun getGuild(): GuildBehavior? =
        (channel as? GuildMessageChannel)?.guild

    /** Extract member information from event data, if that context is available. **/
    public override suspend fun getMember(): MemberBehavior? =
        guild?.getMemberOrNull(genericEvent.interaction.user.id)

    /** Extract user information from event data, if that context is available. **/
    public override suspend fun getUser(): UserBehavior =
        genericEvent.interaction.user
}
