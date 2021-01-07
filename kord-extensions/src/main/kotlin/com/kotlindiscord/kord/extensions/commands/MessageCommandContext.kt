package com.kotlindiscord.kord.extensions.commands

import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.event.message.MessageCreateEvent

public open class MessageCommandContext(
    command: MessageCommand,
    eventObj: MessageCreateEvent,
    commandName: String,
    args: Array<String>
) : CommandContext(command, eventObj, commandName, args) {
    public val event: MessageCreateEvent get() = eventObj as MessageCreateEvent

    public open var channel: MessageChannelBehavior? = null
    public open var guild: Guild? = null
    public open var member: Member? = null
    public open var user: User? = null

    public open lateinit var message: Message

    override suspend fun populate() {
        channel = getChannel()
        guild = getGuild()
        member = getMember()
        user = getUser()

        message = getMessage()
    }

    override suspend fun getChannel(): MessageChannelBehavior = event.message.channel
    override suspend fun getGuild(): Guild? = event.getGuild()
    override suspend fun getMember(): Member? = event.message.getAuthorAsMember()
    override suspend fun getMessage(): Message = event.message
    override suspend fun getUser(): User? = event.message.author
}
