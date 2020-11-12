package com.kotlindiscord.kord.extensions.utils

import com.gitlab.kordlib.core.behavior.channel.createMessage
import com.gitlab.kordlib.core.cache.data.MessageData
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.DmChannel
import com.gitlab.kordlib.core.entity.channel.GuildMessageChannel
import com.gitlab.kordlib.rest.builder.message.MessageCreateBuilder
import com.gitlab.kordlib.rest.request.RestRequestException
import io.ktor.http.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.apache.commons.text.StringTokenizer

private const val DELETE_DELAY = 1000L * 30L  // 30 seconds

/** ID of the message author. **/
val MessageData.authorId: Long get() = author.id

/** Is the message author a bot. **/
val MessageData.authorIsBot: Boolean? get() = author.bot

/**
 * Takes a [Message] object and parses it using a [StringTokenizer].
 *
 * This tokenizes a string, splitting it into an array of strings using whitespace as a
 * delimiter, but supporting quoted tokens (strings between quotes are treated as individual
 * arguments).
 *
 * This is used to create an array of arguments for a command's input.
 *
 * @param message The message to parse
 *
 * @return An array of parsed arguments
 */
fun parseMessage(message: Message): Array<String> = StringTokenizer(message.content, ' ').tokenArray

/**
 * Respond to a message in the channel it was sent to, mentioning the author.
 *
 * @param content Message content.
 */
suspend fun Message.respond(content: String): Message = respond { this.content = content }

/**
 * Respond to a message in the channel it was sent to, mentioning the author.
 *
 * @param builder Builder lambda for populating the message fields.
 */
suspend fun Message.respond(builder: MessageCreateBuilder.() -> Unit): Message {
    val mention = if (this.author != null && this.getChannelOrNull() !is DmChannel) {
        "${this.author!!.mention} "
    } else {
        ""
    }

    return channel.createMessage {
        builder()

        allowedMentions {
            if (author != null) {
                users.add(author!!.id)
            }
        }

        content = "$mention$content"
    }
}

/**
 * Generate the jump URL for this message.
 *
 * @return A clickable URL to jump to this message.
 */
suspend fun Message.getUrl(): String {
    val guild = getGuildOrNull()?.id?.value ?: "@me"

    return "https://discordapp.com/channels/$guild/${channelId.value}/${id.value}"
}

/**
 * Deletes a message, catching and ignoring a HTTP 404 (Not Found) exception.
 */
suspend fun Message.deleteIgnoringNotFound() {
    try {
        this.delete()
    } catch (e: RestRequestException) {
        if (e.code != HttpStatusCode.NotFound.value) {
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
fun Message.deleteWithDelay(millis: Long, retry: Boolean = true): Job {
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
suspend fun Message.requireChannel(
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
        (allowDm && messageChannel is DmChannel)
        || (role != null && topRole != null && topRole >= role)
        || this.channelId == channel.id
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
 * As DMs do not provide access to members and roles, you'll need to provide a lambda that can be used to retrieve
 * the user's top role if you wish to make use of the role bypass.
 *
 * @param role Minimum role required to bypass the channel requirement, or null to disallow any role bypass
 * @param topRoleGetter Lambda used to get the user's top role, or null if role bypass is not desired
 *
 * @return true if the message was posted in an appropriate context, false otherwise
 */
suspend fun Message.requireGuildChannel(role: Role?, topRoleGetter: (suspend (User?) -> Role?)?): Boolean {
    val topRole = topRoleGetter?.invoke(author)

    @Suppress("UnnecessaryParentheses")  // In this case, it feels more readable
    if (
        (role != null && topRole != null && topRole >= role)
        || this.getChannelOrNull() !is DmChannel
    ) return true

    this.respond(
        "This command is not available via private message."
    )

    return false
}
