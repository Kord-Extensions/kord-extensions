@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.base.HelpProvider
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.event.message.MessageCreateEvent

/**
 * Command context object representing the context given to message commands.
 *
 * @property messageCommand Message command object, typed as [MessageCommand] rather than [Command]
 * @property argString String containing the command's unparsed arguments, raw, fresh from Discord itself.
 */
public open class MessageCommandContext<T : Arguments>(
    public val messageCommand: MessageCommand<out T>,
    eventObj: MessageCreateEvent,
    commandName: String,
    argsList: Array<String>,
    public val argString: String
) : CommandContext(messageCommand, eventObj, commandName, argsList) {
    /** Event that triggered this command execution. **/
    public val event: MessageCreateEvent get() = eventObj as MessageCreateEvent

    /** Message channel this command happened in, if any. **/
    public open lateinit var channel: MessageChannelBehavior

    /** Guild this command happened in, if any. **/
    public open var guild: Guild? = null

    /** Guild member responsible for executing this command, if any. **/
    public open var member: Member? = null

    /** User responsible for executing this command, if any (if `null`, it's a webhook). **/
    public open var user: User? = null

    /** Message object containing this command invocation. **/
    public open lateinit var message: Message

    /** Arguments object containing this command's parsed arguments. **/
    public open lateinit var arguments: T

    override suspend fun populate() {
        channel = getChannel()
        guild = getGuild()
        member = getMember()
        user = getUser()

        message = getMessage()
    }

    /** @suppress Internal function **/
    public fun populateArgs(args: T) {
        arguments = args
    }

    override suspend fun getChannel(): MessageChannelBehavior = event.message.channel
    override suspend fun getGuild(): Guild? = event.getGuild()
    override suspend fun getMember(): Member? = event.message.getAuthorAsMember()
    override suspend fun getMessage(): Message = event.message
    override suspend fun getUser(): User? = event.message.author

    /**
     * Generate and send the help embed for this command, using the first loaded extensions that implements
     * [HelpProvider].
     *
     * @return `true` if a help extension exists and help was sent, `false` otherwise.
     */
    public suspend fun sendHelp(): Boolean {
        val helpExtension = this.command.extension.bot.findExtension<HelpProvider>() ?: return false
        val prefix = this.command.extension.bot.messageCommands.getPrefix(event)
        val paginator = helpExtension.getCommandHelpPaginator(event, prefix, messageCommand)

        paginator.send()

        return true
    }
}
