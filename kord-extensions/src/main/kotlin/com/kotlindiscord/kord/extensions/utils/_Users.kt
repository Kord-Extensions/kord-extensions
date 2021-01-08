@file:JvmMultifileClass
@file:JvmName("UserKt")

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
import kotlin.contracts.contract

private const val DISCORD_USERS_URI = "https://discordapp.com/users"

/**
 * A Discord profile link for this user.
 */
public val User.profileLink: String
    get() = "$DISCORD_USERS_URI/${id.asString}/"

/**
 * The creation timestamp for this user.
 */
public val User.createdAt: Instant
    get() = this.id.timeStamp

/**
 * Send a private message to a user, if they have their DMs enabled.
 *
 * @param builder Builder lambda for populating the message fields.
 * @return The sent message, or `null` if the user has their DMs disabled.
 */
public suspend inline fun User.dm(builder: MessageCreateBuilder.() -> Unit): Message? {
    return try {
        this.getDmChannel().createMessage { builder() }
    } catch (e: RestRequestException) {
        when{
            e.hasStatus(HttpStatusCode.Forbidden) -> null
            else -> throw e
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
 * Create a lambda that returns a user's top role, if they're a member of the guild corresponding to the given ID.
 *
 * @param guildID Guild ID to check against.
 *
 * @return Lambda returning the user's top role, or null if they're not on the guild or have no roles.
 */
public fun topRole(guildID: Snowflake): suspend (User) -> Role?
    = { it.asMemberOrNull(guildID)?.getTopRole() }

/**
 * Know if the user is null or is a bot
 * @receiver User or `null` value that will be checked to know if this is a `null` value or discord bot
 * @return `true` if the user is `null` or a bot
 */
public fun User?.isNullOrBot(): Boolean {
    contract {
        returns(false) implies (this@isNullOrBot !== null)
    }

    return this == null || isBot
}
