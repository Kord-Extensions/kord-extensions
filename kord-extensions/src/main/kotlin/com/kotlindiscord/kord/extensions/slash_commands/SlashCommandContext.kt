package com.kotlindiscord.kord.extensions.slash_commands

import com.kotlindiscord.kord.extensions.commands.MessageCommand
import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Channel
import dev.kord.core.event.interaction.InteractionCreateEvent

public open class SlashCommandContext(
    command: MessageCommand,
    event: InteractionCreateEvent,
    commandName: String,
) : CommandContext(command, event, commandName, arrayOf()) {
    public val event: InteractionCreateEvent get() = eventObj as InteractionCreateEvent

    public open lateinit var channel: Channel
    public open lateinit var guild: Guild
    public open lateinit var member: MemberBehavior
    public open lateinit var user: UserBehavior

    override suspend fun populate() {
        channel = getChannel()
        guild = getGuild()
        member = getMember()
        user = getUser()
    }

    override suspend fun getChannel(): Channel = event.interaction.getChannel()
    override suspend fun getGuild(): Guild = event.interaction.getGuild()
    override suspend fun getMember(): MemberBehavior = event.interaction.member
    override suspend fun getMessage(): MessageBehavior? = null
    override suspend fun getUser(): UserBehavior = event.interaction.member
}
