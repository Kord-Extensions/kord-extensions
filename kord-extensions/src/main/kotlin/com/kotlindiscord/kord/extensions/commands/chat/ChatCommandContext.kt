@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.commands.chat

import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.extensions.base.HelpProvider
import com.kotlindiscord.kord.extensions.pagination.MessageButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent

/**
 * Command context object representing the context given to chat commands.
 *
 * @property messageCommand Chat command object
 * @param parser String parser instance, if any - will be `null` if this isn't a chat command.
 * @property argString String containing the command's unparsed arguments, raw, fresh from Discord itself.
 */
@ExtensionDSL
public open class ChatCommandContext<T : Arguments>(
    public val messageCommand: ChatCommand<out T>,
    eventObj: MessageCreateEvent,
    commandName: String,
    public open val parser: StringParser?,
    public val argString: String
) : CommandContext(messageCommand, eventObj, commandName) {
    /** Event that triggered this command execution. **/
    public val event: MessageCreateEvent get() = eventObj as MessageCreateEvent

    /** Message channel this command happened in, if any. **/
    public open lateinit var channel: MessageChannelBehavior

    /** Guild this command happened in, if any. **/
    public open var guild: GuildBehavior? = null

    /** Guild member responsible for executing this command, if any. **/
    public open var member: MemberBehavior? = null

    /** User responsible for executing this command, if any (if `null`, it's a webhook). **/
    public open var user: UserBehavior? = null

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
    override suspend fun getGuild(): GuildBehavior? = event.guildId?.let { GuildBehavior(it, event.kord) }
    override suspend fun getMember(): MemberBehavior? = event.member
    override suspend fun getUser(): UserBehavior? = event.message.author

    /** Extract message information from event data, if that context is available. **/
    public open suspend fun getMessage(): Message = event.message

    /**
     * Convenience function to create a button paginator using a builder DSL syntax. Handles the contextual stuff for
     * you.
     */
    public suspend fun paginator(
        defaultGroup: String = "",

        pingInReply: Boolean = true,
        targetChannel: MessageChannelBehavior? = null,
        targetMessage: Message? = null,

        body: suspend PaginatorBuilder.() -> Unit
    ): MessageButtonPaginator {
        val builder = PaginatorBuilder(getLocale(), defaultGroup = defaultGroup)

        body(builder)

        return MessageButtonPaginator(pingInReply, targetChannel, targetMessage, builder)
    }

    /**
     * Generate and send the help embed for this command, using the first loaded extensions that implements
     * [HelpProvider].
     *
     * @return `true` if a help extension exists and help was sent, `false` otherwise.
     */
    public suspend fun sendHelp(): Boolean {
        val helpExtension = this.command.extension.bot.findExtension<HelpProvider>() ?: return false
        val paginator = helpExtension.getCommandHelpPaginator(this, messageCommand)

        paginator.send()

        return true
    }

    /**
     * Convenience function allowing for message responses with translated content.
     */
    public suspend fun Message.respondTranslated(
        key: String,
        replacements: Array<Any?> = arrayOf(),
        useReply: Boolean = true
    ): Message = respond(translate(key, replacements), useReply)
}
