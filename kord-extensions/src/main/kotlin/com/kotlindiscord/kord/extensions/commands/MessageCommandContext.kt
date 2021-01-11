package com.kotlindiscord.kord.extensions.commands

import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.event.message.MessageCreateEvent

/**
 * Command context object representing the context given to message commands.
 */
public open class MessageCommandContext(
    command: MessageCommand,
    eventObj: MessageCreateEvent,
    commandName: String,
    args: Array<String>
) : CommandContext(command, eventObj, commandName, args) {
    /** Event that triggered this command execution. **/
    public val event: MessageCreateEvent get() = eventObj as MessageCreateEvent

    /** Message channel this command happened in, if any. **/
    public open var channel: MessageChannelBehavior? = null

    /** Guild this command happened in, if any. **/
    public open var guild: Guild? = null

    /** Guild member responsible for executing this command, if any. **/
    public open var member: Member? = null

    /** User responsible for executing this command, if any (if `null`, it's a webhook). **/
    public open var user: User? = null

    /** Message object containing this command invocation. **/
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
