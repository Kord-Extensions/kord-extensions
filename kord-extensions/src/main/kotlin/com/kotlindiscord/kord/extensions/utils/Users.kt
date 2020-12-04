package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.MessageCreateBuilder
import dev.kord.rest.request.RestRequestException
import io.ktor.http.*
import java.time.Instant

/**
 * Send a private message to a user, if they have their DMs enabled.
 *
 * @param builder Builder lambda for populating the message fields.
 * @return The sent message, or `null` if the user has their DMs disabled.
 */
public suspend fun User.dm(builder: MessageCreateBuilder.() -> Unit): Message? {
    return try {
        this.getDmChannel().createMessage { builder() }
    } catch (e: RestRequestException) {
        if (e.status.code == HttpStatusCode.Forbidden.value) {
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
public suspend fun User.dm(content: String): Message? = this.dm { this.content = content }

/**
 * The creation timestamp for this user.
 */
public val User.createdAt: Instant get() = this.id.timeStamp

/**
 * Create a lambda that returns a user's top role, if they're a member of the guild corresponding to the given ID.
 *
 * @param guildID Guild ID to check against.
 *
 * @return Lambda returning the user's top role, or null if they're not on the guild or have no roles.
 */
public fun topRole(guildID: Snowflake): suspend (User) -> Role? {
    suspend fun inner(user: User) = user.asMemberOrNull(guildID)?.getTopRole()

    return ::inner
}
