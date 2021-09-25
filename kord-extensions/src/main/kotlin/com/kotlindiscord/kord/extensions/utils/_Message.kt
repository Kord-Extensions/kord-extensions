package com.kotlindiscord.kord.extensions.utils

import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.kord.common.entity.DiscordPartialMessage
import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.PublicFollowupMessageBehavior
import dev.kord.core.behavior.reply
import dev.kord.core.cache.data.MessageData
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.*
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.allowedMentions
import dev.kord.rest.request.RestRequestException
import io.ktor.http.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.apache.commons.text.StringTokenizer
import org.apache.commons.text.matcher.StringMatcherFactory

private val logger = KotlinLogging.logger {}

private const val DELETE_DELAY = 1000L * 30L  // 30 seconds
private const val DISCORD_CHANNEL_URI = "https://discord.com/channels"

/**
 * Deletes a message, catching and ignoring a HTTP 404 (Not Found) exception.
 */
public suspend fun MessageBehavior.deleteIgnoringNotFound() {
    try {
        delete()
    } catch (e: RestRequestException) {
        if (e.hasNotStatus(HttpStatusCode.NotFound)) {
            throw e
        }
    }
}

/**
 * Deletes a public follow-up, catching and ignoring a HTTP 404 (Not Found) exception.
 */
public suspend fun PublicFollowupMessageBehavior.deleteIgnoringNotFound() {
    try {
        delete()
    } catch (e: RestRequestException) {
        if (e.hasNotStatus(HttpStatusCode.NotFound)) {
            throw e
        }
    }
}

/**
 * Deletes a message after a delay.
 *
 * This function **does not block**.
 *
 * @param millis The delay before deleting the message, in milliseconds.
 * @return Job spawned by the CoroutineScope.
 */
public fun MessageBehavior.delete(millis: Long, retry: Boolean = true): Job {
    return kord.launch {
        delay(millis)

        try {
            this@delete.deleteIgnoringNotFound()
        } catch (e: RestRequestException) {
            val message = this@delete

            if (retry) {
                logger.debug(e) { "Failed to delete message, retrying: $message" }
                this@delete.delete(millis, false)
            } else {
                logger.error(e) { "Failed to delete message: $message" }
            }
        }
    }
}

/**
 * Deletes a public follow-up after a delay.
 *
 * This function **does not block**.
 *
 * @param millis The delay before deleting the message, in milliseconds.
 * @return Job spawned by the CoroutineScope.
 */
public fun PublicFollowupMessageBehavior.delete(millis: Long, retry: Boolean = true): Job {
    return kord.launch {
        delay(millis)

        try {
            this@delete.deleteIgnoringNotFound()
        } catch (e: RestRequestException) {
            val message = this@delete

            if (retry) {
                logger.debug(e) { "Failed to delete message, retrying: $message" }
                this@delete.delete(millis, false)
            } else {
                logger.error(e) { "Failed to delete message: $message" }
            }
        }
    }
}

/**
 * Add a reaction to this message, using the Unicode emoji represented by the given string.
 *
 * @param emoji Emoji to add to the message.
 */
public suspend inline fun MessageBehavior.addReaction(emoji: String): Unit = addReaction(emoji.toReaction())

/**
 * Remove a reaction from this message, using a guild's custom emoji object.
 *
 * @param emoji Emoji to remove from the message.
 */
public suspend inline fun MessageBehavior.deleteReaction(userId: Snowflake, emoji: GuildEmoji): Unit =
    deleteReaction(userId, emoji.toReaction())

/**
 * Remove a reaction from this message, using the Unicode emoji represented by the given string.
 *
 * @param emoji Emoji to remove from message.
 */
public suspend inline fun MessageBehavior.deleteReaction(userId: Snowflake, emoji: String): Unit =
    deleteReaction(userId, emoji.toReaction())

/**
 * Remove a reaction from this message, using a guild's custom emoji object.
 *
 * @param emoji Emoji to remove from the message.
 */
public suspend inline fun MessageBehavior.deleteReaction(emoji: GuildEmoji): Unit = deleteReaction(emoji.toReaction())

/**
 * Remove a reaction from this message, using the Unicode emoji represented by the given string.
 *
 * @param emoji Emoji to remove from the message.
 */
public suspend inline fun MessageBehavior.deleteReaction(unicode: String): Unit = deleteReaction(unicode.toReaction())

/**
 * Remove a reaction from this message belonging to the bot, using a guild's custom emoji object.
 *
 * @param emoji Emoji to remove from the message.
 */
public suspend inline fun MessageBehavior.deleteOwnReaction(emoji: GuildEmoji): Unit =
    deleteOwnReaction(emoji.toReaction())

/**
 * Remove a reaction from this message belonging to the bot, using the Unicode emoji represented by the given string.
 *
 * @param emoji Emoji to remove from the message.
 */
public suspend inline fun MessageBehavior.deleteOwnReaction(unicode: String): Unit =
    deleteOwnReaction(unicode.toReaction())

/** Message author's ID. **/
public val MessageData.authorId: Snowflake
    get() = author.id

/** Whether the message author is a bot. **/
public val MessageData.authorIsBot: Boolean
    get() = author.bot.discordBoolean

/**
 * Takes a [Message] object and parses it using a [StringTokenizer].
 *
 * This tokenizes a string, splitting it into an array of strings using whitespace as a
 * delimiter, but supporting quoted tokens (strings between quotes are treated as individual
 * arguments).
 *
 * This is used to create an array of arguments for a command's input.
 *
 * @param delimiters An array of delimiters to split with, if not just a space
 * @param quotes An array of quote characters, if you need something other than just `'` and `"`
 *
 * @return An array of parsed arguments
 */
public fun Message.parse(
    delimiters: CharArray = charArrayOf(' '),
    quotes: CharArray = charArrayOf('\'', '"')
): Array<String> =
    StringTokenizer(content)
        .setDelimiterMatcher(StringMatcherFactory.INSTANCE.charSetMatcher(delimiters.joinToString()))
        .setQuoteMatcher(StringMatcherFactory.INSTANCE.charSetMatcher(quotes.joinToString()))
        .tokenArray

/**
 * Respond to a message in the channel it was sent to, mentioning the author.
 *
 * @param useReply Whether to use Discord's replies feature to respond, instead of a mention. Defaults to `true`.
 * @param pingInReply When [useReply] is true, whether to also ping the user in the reply. Ignored if [useReply] is
 * false.
 * @param content Message content.
 *
 * @return The newly-created response message.
 */
public suspend fun Message.respond(content: String, useReply: Boolean = true, pingInReply: Boolean = true): Message =
    respond(useReply, pingInReply) { this.content = content }

/**
 * Respond to a message in the channel it was sent to, mentioning the author.
 *
 * @param useReply Whether to use Discord's replies feature to respond, instead of a mention. Defaults to `true`.
 * @param pingInReply When [useReply] is true, whether to also ping the user in the reply. Ignored if [useReply] is
 * false.
 * @param builder Builder lambda for populating the message fields.
 *
 * @return The newly-created response message.
 */
public suspend fun Message.respond(
    useReply: Boolean = true,
    pingInReply: Boolean = true,
    builder: suspend MessageCreateBuilder.() -> Unit
): Message {
    val author = this.author
    val innerBuilder: suspend MessageCreateBuilder.() -> Unit = {
        builder()

        allowedMentions {
            when {
                useReply && pingInReply -> repliedUser = true
                author != null && !pingInReply -> users.add(author.id)
            }
        }

        val mention = if (author != null && !useReply && getChannelOrNull() !is DmChannel) {
            author.mention
        } else {
            ""
        }

        val contentWithMention = "$mention ${content ?: ""}".trim()

        if (contentWithMention.isNotEmpty()) {
            content = contentWithMention
        }
    }

    return if (useReply) {
        reply { innerBuilder() }
    } else {
        channel.createMessage { innerBuilder() }
    }
}

/**
 * Generate the jump URL for this message.
 *
 * @return A clickable URL to jump to this message.
 */
public fun Message.getJumpUrl(): String =
    "$DISCORD_CHANNEL_URI/${data.guildId.value?.value ?: "@me"}/${channelId.value}/${id.value}"

/**
 * Generate the jump URL for this message.
 *
 * @return A clickable URL to jump to this message.
 */
public fun DiscordPartialMessage.getJumpUrl(): String =
    "$DISCORD_CHANNEL_URI/${guildId.value?.value ?: "@me"}/${channelId.value}/${id.value}"

/**
 * Check that this message happened in either the given channel or a DM, or that the author is at least a given role.
 *
 * If none of those things are true, a response message will be created instructing the user to try again in
 * the given channel.
 *
 * @param channel Channel to require the message to have been sent in
 * @param role Minimum role required to bypass the channel requirement, or null to disallow any role bypass
 * @param delay How long (in milliseconds) to wait before deleting the response message (30 seconds by default)
 * @param allowDm Whether to treat a DM as an acceptable context
 * @param deleteOriginal Whether to delete the original message, using the given delay (true by default)
 * @param deleteResponse Whether to delete the response, using the given delay (true by default)
 *
 * @return true if the message was posted in an appropriate context, false otherwise
 */
public suspend fun Message.requireChannel(
    context: CommandContext,
    channel: GuildMessageChannel,
    role: Role? = null,
    delay: Long = DELETE_DELAY,
    allowDm: Boolean = true,
    deleteOriginal: Boolean = true,
    deleteResponse: Boolean = true
): Boolean {
    val topRole = if (getGuildOrNull() == null) {
        null
    } else {
        getAuthorAsMember()!!.getTopRole()
    }

    val messageChannel = getChannelOrNull()

    @Suppress("UnnecessaryParentheses")  // In this case, it feels more readable
    if (
        (allowDm && messageChannel is DmChannel) ||
        (role != null && topRole != null && topRole >= role) ||
        channelId == channel.id
    ) return true

    val response = respond(
        context.translate("utils.message.useThisChannel", replacements = arrayOf(channel.mention))
    )

    if (deleteResponse) response.delete(delay)
    if (deleteOriginal && messageChannel !is DmChannel) this.delete(delay)

    return false
}

/**
 * Check that this message happened in a guild channel.
 *
 * If it didn't, a response message will be created instructing the user that the current command can't be used via a
 * private message.
 *
 * @param role Minimum role required to bypass the channel requirement, or null to disallow any role bypass
 *
 * @return true if the message was posted in an appropriate context, false otherwise
 */
public suspend fun Message.requireGuildChannel(
    context: CommandContext,
    role: Role? = null
): Boolean {
    val author = this.author
    val guild = getGuildOrNull()

    val topRole = if (author != null && guild != null) {
        author.asMemberOrNull(guild.id)
    } else {
        null
    }

    @Suppress("UnnecessaryParentheses")  // In this case, it feels more readable
    if (
        (role != null && topRole != null && topRole >= role) ||
        getChannelOrNull() !is DmChannel
    ) return true

    respond(context.translate("utils.message.commandNotAvailableInDm"))
    return false
}

/**
 * Check that this message happened in a guild channel.
 *
 * If it didn't, a response message will be created instructing the user that the current command can't be used via a
 * private message.
 *
 * As DMs do not provide access to members and roles, you'll need to provide a lambda that can be used to retrieve
 * the user's top role if you wish to make use of the role bypass.
 *
 * @param role Minimum role required to bypass the channel requirement, omit to disallow a role bypass
 * @param guild Guild to check for the user's top role, omit to disallow a role bypass
 *
 * @return true if the message was posted in an appropriate context, false otherwise
 */
public suspend fun Message.requireGuildChannel(
    context: CommandContext,
    role: Role? = null,
    guild: Guild? = null
): Boolean {
    val author = this.author
    val topRole = if (author != null) {
        guild?.getMember(author.id)?.getTopRole()
    } else {
        null
    }

    @Suppress("UnnecessaryParentheses")  // In this case, it feels more readable
    if (
        (role != null && topRole != null && topRole >= role) ||
        getChannelOrNull() !is DmChannel
    ) return true

    respond(context.translate("utils.message.commandNotAvailableInDm"))
    return false
}

/** Whether this message was published to the guilds that are following its channel. **/
public val Message.isPublished: Boolean
    get() =
        data.flags.value?.contains(MessageFlag.CrossPosted) == true

/** Whether this message was sent from a different guild's followed announcement channel. **/
public val Message.isCrossPost: Boolean
    get() =
        data.flags.value?.contains(MessageFlag.IsCrossPost) == true

/** Whether this message's embeds should be serialized. **/
public val Message.suppressEmbeds: Boolean
    get() =
        data.flags.value?.contains(MessageFlag.SuppressEmbeds) == true

/** When [isCrossPost], whether the source message has been deleted from the original guild. **/
public val Message.originalMessageDeleted: Boolean
    get() =
        data.flags.value?.contains(MessageFlag.SourceMessageDeleted) == true

/** Whether this message came from Discord's urgent message system. **/
public val Message.isUrgent: Boolean
    get() =
        data.flags.value?.contains(MessageFlag.Urgent) == true

/** Whether this is an ephemeral message from the Interactions system. **/
public val Message.isEphemeral: Boolean
    get() =
        data.flags.value?.contains(MessageFlag.Ephemeral) == true

/**
 * Wait for a message, using the given timeout (in milliseconds ) and filter function.
 *
 * Will return `null` if no message is found before the timeout.
 */
public suspend fun waitForMessage(
    timeout: Long,
    filter: (suspend (MessageCreateEvent).() -> Boolean) = { true }
): Message? {
    val kord = getKoin().get<Kord>()
    val event = kord.waitFor(timeout, filter)

    return event?.message
}

/**
 * Wait for a message from a user, using the given timeout (in milliseconds) and extra filter function.
 *
 * Will return `null` if no message is found before the timeout.
 */
public suspend fun UserBehavior.waitForMessage(
    timeout: Long,
    filter: (suspend (MessageCreateEvent).() -> Boolean) = { true }
): Message? {
    val kord = getKoin().get<Kord>()
    val event = kord.waitFor<MessageCreateEvent>(timeout) {
        message.author?.id == id &&
            filter()
    }

    return event?.message
}

/**
 * Wait for a message in this channel, using the given timeout (in milliseconds) and extra filter function.
 *
 * Will return `null` if no message is found before the timeout.
 */
public suspend fun MessageChannelBehavior.waitForMessage(
    timeout: Long,
    filter: (suspend (MessageCreateEvent).() -> Boolean) = { true }
): Message? {
    val kord = getKoin().get<Kord>()
    val event = kord.waitFor<MessageCreateEvent>(timeout) {
        message.channelId == id &&
            filter()
    }

    return event?.message
}

/**
 * Wait for a message in reply to this one, using the given timeout (in milliseconds) and extra filter function.
 *
 * Will return `null` if no message is found before the timeout.
 */
public suspend fun MessageBehavior.waitForReply(
    timeout: Long,
    filter: (suspend (MessageCreateEvent).() -> Boolean) = { true }
): Message? {
    val kord = getKoin().get<Kord>()
    val event = kord.waitFor<MessageCreateEvent>(timeout) {
        message.messageReference?.message?.id == id &&
            filter()
    }

    return event?.message
}

/**
 * Wait for a message by the user that invoked this command, in the channel it was invoked in, using the given
 * timeout (in milliseconds) and extra filter function.
 *
 * Will return `null` if no message is found before the timeout.
 */
public suspend fun CommandContext.waitForResponse(
    timeout: Long,
    filter: (suspend (MessageCreateEvent).() -> Boolean) = { true }
): Message? {
    val kord = com.kotlindiscord.kord.extensions.utils.getKoin().get<Kord>()
    val event = kord.waitFor<MessageCreateEvent>(timeout) {
        message.author?.id == getUser()?.id &&
            message.channelId == getChannel()?.id &&
            filter()
    }

    return event?.message
}
