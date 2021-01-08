@file:JvmMultifileClass
@file:JvmName("MessageKt")

package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.reply
import dev.kord.core.cache.data.MessageData
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.rest.builder.message.MessageCreateBuilder
import dev.kord.rest.request.RestRequestException
import io.ktor.http.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.apache.commons.text.StringTokenizer
import org.apache.commons.text.matcher.StringMatcherFactory

/**
 * Time to delete information
 */
private const val DELETE_DELAY = 1000L * 30L  // 30 seconds

/**
 * URI of Discord channels
 */
private const val DISCORD_CHANNEL_URI = "https://discordapp.com/channels"

/**
 * Deletes a message, catching and ignoring a HTTP 404 (Not Found) exception.
 */
public suspend fun MessageBehavior.deleteIgnoringNotFound() {
    try {
        this.delete()
    } catch (e: RestRequestException) {
        if(e.isNotStatus(HttpStatusCode.NotFound)){
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
public fun MessageBehavior.deleteWithDelay(millis: Long, retry: Boolean = true): Job {
    val logger = KotlinLogging.logger {}

    return this.kord.launch {
        delay(millis)

        try {
            this@deleteWithDelay.deleteIgnoringNotFound()
        } catch (e: RestRequestException) {
            val message = this@deleteWithDelay

            if (retry) {
                logger.debug(e) {
                    "Failed to delete message, retrying: $message"
                }

                this@deleteWithDelay.deleteWithDelay(millis, false)
            } else {
                logger.error(e) {
                    "Failed to delete message: $message"
                }
            }
        }
    }
}

/**
 * Requests to add an emoji with unicode format to this message.
 * @param unicode Emoji that will be added to the message
 * @throws [RestRequestException] if something went wrong during the request.
 */
public suspend inline fun MessageBehavior.addReaction(unicode: String): Unit
    = addReaction(unicode.toReaction())

/**
 * Requests to remove an [emoji] to this message.
 * @param emoji Emojis that will be removed to the message
 * @throws [RestRequestException] if something went wrong during the request.
 */
public suspend inline fun MessageBehavior.deleteReaction(userId: Snowflake, emoji: GuildEmoji): Unit
    = deleteReaction(userId, emoji.toReaction())

/**
 * Requests to delete an [emoji] to this message.
 * @param emoji Emoji that will be deleted to the message
 * @throws [RestRequestException] if something went wrong during the request.
 */
public suspend inline fun MessageBehavior.deleteReaction(emoji: GuildEmoji): Unit =
    this.deleteReaction(emoji.toReaction())

/**
 * Requests to delete an emoji with unicode format to this message.
 * @param unicode Emoji that will be deleted to the message
 * @throws [RestRequestException] if something went wrong during the request.
 */
public suspend inline fun MessageBehavior.deleteReaction(unicode: String): Unit =
    this.deleteReaction(unicode.toReaction())

/**
 * Requests to remove an [emoji] with unicode format to this message.
 * @param emoji Emojis that will be removed to the message
 * @throws [RestRequestException] if something went wrong during the request.
 */
public suspend inline fun MessageBehavior.deleteReaction(userId: Snowflake, emoji: String): Unit
    = deleteReaction(userId, emoji.toReaction())

/**
 * Requests to remove an [emoji] from the own bot to this message.
 * @param emoji Emoji that will be removed to the message
 * @throws [RestRequestException] if something went wrong during the request.
 */
public suspend inline fun MessageBehavior.deleteOwnReaction(emoji: GuildEmoji): Unit
    = deleteOwnReaction(emoji.toReaction())

/**
 * Requests to remove emoji with unicode format from the own bot to this message.
 * @param unicode Emoji that will be removed to the message
 * @throws [RestRequestException] if something went wrong during the request.
 */
public suspend inline fun MessageBehavior.deleteOwnReaction(unicode: String): Unit
    = deleteOwnReaction(unicode.toReaction())

/** ID of the message author. **/
public val MessageData.authorId: Snowflake
    get() = author.id

/** Is the message author a bot. **/
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
): Array<String>
    = StringTokenizer(content)
    .setDelimiterMatcher(StringMatcherFactory.INSTANCE.charSetMatcher(delimiters.joinToString()))
    .setQuoteMatcher(StringMatcherFactory.INSTANCE.charSetMatcher(quotes.joinToString()))
    .tokenArray

/**
 * Respond to a message in the channel it was sent to, mentioning the author.
 *
 * @param useReply Whether to use Discord's replies feature to respond, instead of a mention. Defaults to `true`.
 * @param content Message content.
 *
 * @return The newly-created response message.
 */
public suspend fun Message.respond(content: String, useReply: Boolean = true): Message =
    respond(useReply) { this.content = content }

/**
 * Respond to a message in the channel it was sent to, mentioning the author.
 *
 * @param useReply Whether to use Discord's replies feature to respond, instead of a mention. Defaults to `true`.
 * @param builder Builder lambda for populating the message fields.
 *
 * @return The newly-created response message.
 */
public suspend fun Message.respond(
    useReply: Boolean = true,
    builder: suspend MessageCreateBuilder.() -> Unit
): Message {
    val mention = if (this.author != null && this.getChannelOrNull() !is DmChannel && !useReply) {
        "${this.author!!.mention} "
    } else {
        ""
    }

    val innerBuilder: suspend MessageCreateBuilder.() -> Unit = {
        builder()

        allowedMentions {
            if (author != null && !useReply) {
                users.add(author!!.id)
            }

            if (useReply) {
                repliedUser = true
            }
        }

        val contentWithMention = "$mention${content ?: ""}"

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
public suspend fun Message.getUrl(): String {
    val guild = getGuildOrNull()?.id?.asString ?: "@me"

    return "$DISCORD_CHANNEL_URI/$guild/${channelId.value}/${id.value}"
}

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
    channel: GuildMessageChannel,
    role: Role? = null,
    delay: Long = DELETE_DELAY,
    allowDm: Boolean = true,
    deleteOriginal: Boolean = true,
    deleteResponse: Boolean = true
): Boolean {
    val topRole = if (getGuildOrNull() != null) {
        this.getAuthorAsMember()!!.getTopRole()
    } else {
        null
    }

    val messageChannel = this.getChannelOrNull()

    @Suppress("UnnecessaryParentheses")  // In this case, it feels more readable
    if (
        (allowDm && messageChannel is DmChannel) ||
        (role != null && topRole != null && topRole >= role) ||
        this.channelId == channel.id
    ) return true

    val response = this.respond(
        "Please use ${channel.mention} for this command."
    )

    if (deleteResponse) response.deleteWithDelay(delay)
    if (deleteOriginal && messageChannel !is DmChannel) this.deleteWithDelay(delay)

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
public suspend fun Message.requireGuildChannel(role: Role? = null): Boolean {
    val topRole = if (author != null && getGuildOrNull() != null) author!!.asMemberOrNull(getGuild().id) else null

    @Suppress("UnnecessaryParentheses")  // In this case, it feels more readable
    if (
        (role != null && topRole != null && topRole >= role) ||
        this.getChannelOrNull() !is DmChannel
    ) return true

    this.respond(
        "This command is not available via private message."
    )

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
public suspend fun Message.requireGuildChannel(role: Role? = null, guild: Guild? = null): Boolean {
    val topRole = if (author != null) guild?.getMember(this.author!!.id)?.getTopRole() else null

    @Suppress("UnnecessaryParentheses")  // In this case, it feels more readable
    if (
        (role != null && topRole != null && topRole >= role) ||
        this.getChannelOrNull() !is DmChannel
    ) return true

    this.respond(
        "This command is not available via private message."
    )

    return false
}
