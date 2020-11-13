package com.kotlindiscord.kord.extensions.utils

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.channel.createMessage
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.rest.builder.message.MessageCreateBuilder
import com.gitlab.kordlib.rest.request.RestRequestException
import io.ktor.http.*
import java.time.Instant

/**
 * Send a private message to a user, if they have their DMs enabled.
 *
 * @param builder Builder lambda for populating the message fields.
 * @return The sent message, or `null` if the user has their DMs disabled.
 */
suspend fun User.dm(builder: MessageCreateBuilder.() -> Unit): Message? {
    return try {
        this.getDmChannel().createMessage { builder() }
    } catch (e: RestRequestException) {
        if (e.code == HttpStatusCode.Forbidden.value) {
            // They have DMs disabled
            null
        } else {
            throw e
        }
    }
}

/**
 * Send a private message to a user, if they have their DMs enabled.
 *
 * @param content Message content.
 * @return The sent message, or `null` if the user has their DMs disabled.
 */
suspend fun User.dm(content: String) = this.dm { this.content = content }

/**
 * The creation timestamp for this user.
 */
val User.createdAt: Instant get() = this.id.timeStamp

/**
 * Create a lambda that returns a user's top role, if they're a member of the guild corresponding to the given ID.
 *
 * @param guildID Guild ID to check against.
 *
 * @return Lambda returning the user's top role, or null if they're not on the guild or have no roles.
 */
fun topRole(guildID: Snowflake): suspend (User) -> Role? {
    suspend fun inner(user: User) = user.asMemberOrNull(guildID)?.getTopRole()

    return ::inner
}
