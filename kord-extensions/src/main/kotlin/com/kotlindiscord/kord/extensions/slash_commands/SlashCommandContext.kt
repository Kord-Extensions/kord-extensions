package com.kotlindiscord.kord.extensions.slash_commands

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.InteractionResponseBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Channel
import dev.kord.core.event.interaction.InteractionCreateEvent

@OptIn(KordPreview::class)
public open class SlashCommandContext<R : Arguments>(
    public val slashCommand: SlashCommand<out R>,
    event: InteractionCreateEvent,
    commandName: String,
    public val interactionResponse: InteractionResponseBehavior
) : CommandContext(slashCommand, event, commandName, arrayOf()) {
    public val event: InteractionCreateEvent get() = eventObj as InteractionCreateEvent

    public open lateinit var channel: Channel
    public open lateinit var guild: Guild
    public open lateinit var member: MemberBehavior
    public open lateinit var user: UserBehavior
    public open lateinit var arguments: R

    override suspend fun populate() {
        channel = getChannel()
        guild = getGuild()
        member = getMember()
        user = getUser()
    }

    public fun populateArgs(args: R) {
        arguments = args
    }

    override suspend fun getChannel(): Channel = event.interaction.getChannel()
    override suspend fun getGuild(): Guild = event.interaction.getGuild()
    override suspend fun getMember(): MemberBehavior = event.interaction.member
    override suspend fun getMessage(): MessageBehavior? = null
    override suspend fun getUser(): UserBehavior = event.interaction.member
}
