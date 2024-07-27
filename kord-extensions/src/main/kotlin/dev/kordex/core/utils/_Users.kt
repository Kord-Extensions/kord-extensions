/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.utils

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.request.RestRequestException
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlin.contracts.contract

private const val DISCORD_USERS_URI = "https://discord.com/users"

/**
 * If the given user still has a discriminator, return `"username#discrim"`.
 * Otherwise, only return their new-style username.
 *
 * @return User's tag or username, depending on whether they've migrated or not.
 */
@Deprecated(
	"As it appears that bots will keep their discriminators, their use is no longer deprecated. Instead, " +
		"use the [tag] property, which will return only the username if the user doesn't have a discriminator, or " +
		"`user#discriminator` if they do.",

	level = DeprecationLevel.ERROR,
	replaceWith = ReplaceWith("tag")
)
public fun User.tagOrUsername(): String =
	if (discriminator == "0") {
		username
	} else {
		tag
	}

/**
 * The user's Discord profile URL.
 */
public val User.profileLink: String
	get() = "$DISCORD_USERS_URI/$id/"

/**
 * The user's creation timestamp.
 */
public val User.createdAt: Instant
	get() = this.id.timestamp

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
		if (e.hasStatus(HttpStatusCode.Forbidden)) {
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
 * Create a lambda that returns a user's top role, if they're a member of the guild corresponding to the given ID.
 *
 * @param guildID Guild ID to check against.
 *
 * @return Lambda returning the user's top role, or null if they're not on the guild or have no roles.
 */
public fun topRole(guildID: Snowflake): suspend (User) -> Role? = { it.asMemberOrNull(guildID)?.getTopRole() }

/**
 * Check whether the given user is `null` or a bot.
 *
 * @receiver Nullable [User] to check.
 * @return `true` if the user is `null` or a bot, `false` otherwise.
 */
public fun User?.isNullOrBot(): Boolean {
	contract {
		returns(false) implies (this@isNullOrBot !== null)
	}

	return this == null || isBot
}
