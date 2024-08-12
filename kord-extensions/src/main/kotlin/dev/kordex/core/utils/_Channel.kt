/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.core.utils

import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.*
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.Webhook
import dev.kord.core.entity.channel.CategorizableChannel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.channel.TopGuildChannel
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.rest.Image
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.firstOrNull

private val logger = KotlinLogging.logger {}

/**
 * Ensure a webhook is created for the bot in a given channel, and return it.
 *
 * If a webhook already exists with the given name, it will be returned instead.
 *
 * @param channelObj Channel to create the webhook for.
 * @param name Name for the webhook
 * @param logoFormat Image.Format instance representing the format of the logo - defaults to PNG
 * @param logo Callable returning logo image data for the newly created webhook
 *
 * @return Webhook object for the newly created webhook, or the existing one if it's already there.
 */
public suspend fun ensureWebhook(
	channelObj: TopGuildMessageChannel,
	name: String,
	logoFormat: Image.Format = Image.Format.PNG,
	logo: (suspend () -> ByteArray)? = null,
): Webhook {
	val webhook = channelObj.webhooks.firstOrNull { it.name == name }

	if (webhook != null) {
		return webhook
	}

	val guild = channelObj.guild.asGuild()

	logger.info { "Creating webhook for channel: #${channelObj.name} (Guild: ${guild.name}" }

	return channelObj.createWebhook(name) {
		if (logo != null) {
			this.avatar = Image.raw(logo.invoke(), logoFormat)
		}
	}
}

/**
 * Given a guild channel, attempt to calculate the effective permissions for the member corresponding with
 * the given ID, checking the parent channel if this one happens to be a thread.
 *
 * @param memberId Member ID to calculate for
 */
public suspend fun GuildChannel.permissionsForMember(memberId: Snowflake): Permissions = when (this) {
	is TopGuildChannel -> getEffectivePermissions(memberId)
	is ThreadChannel -> getParent().getEffectivePermissions(memberId)

	else -> error("Unsupported channel type for channel: $this")
}

/**
 * Given a guild channel, attempt to calculate the effective permissions for given user, checking the
 * parent channel if this one happens to be a thread.
 *
 * @param user User to calculate for
 */
public suspend fun GuildChannel.permissionsForMember(user: UserBehavior): Permissions =
	permissionsForMember(user.id)

/**
 * Convenience function that returns the thread's parent message, if it was created from one.
 *
 * If it wasn't, the parent channel is a forum, or the parent channel can't be found, this function returns `null`.
 */
public suspend fun ThreadChannel.getParentMessage(): Message? {
	val parentChannel = getParentOrNull() as? MessageChannelBehavior ?: return null

	return parentChannel.getMessageOrNull(this.id)
}

// region: Channel position utils

/**
 * Get the corresponding top guild channel for the given channel.
 *
 * * If this is a thread channel, return the parent channel.
 * * If this is a top guild channel, return it.
 * * Otherwise, return `null`.
 */
public fun GuildChannelBehavior.getTopChannel(): TopGuildChannelBehavior? = when (this) {
	is ThreadChannelBehavior -> this.parent
	is TopGuildChannelBehavior -> this

	else -> null
}

/**
 * Get the corresponding category channel for the given channel.
 *
 * * If this is a thread channel, return the parent channel's category.
 * * If this is a categorizable channel, return its parent.
 * * If this is a category, return it.
 * * Otherwise, return `null`.
 */
public fun GuildChannelBehavior.getCategory(): CategoryBehavior? = when (this) {
	is ThreadChannelBehavior -> (this.parent as? CategorizableChannel)?.category
	is CategorizableChannel -> this.category
	is CategoryBehavior -> this

	else -> null
}

/**
 * Check whether the receiver ([this]) is above the given [other] channel in the channel list.
 *
 * This function attempts to calculate this the same way the Discord client does:
 *
 * * Channels outside a category are always at the top.
 * * Compare channels within the same category (or both at the top) by position.
 * * Compare parent categories by position.
 *
 * This function throws an exception if the comparison doesn't make sense.
 * This shouldn't ever happen unless Kord's type system breaks.
 */
public suspend fun GuildChannelBehavior.isAbove(other: GuildChannelBehavior): Boolean {
	val thisChannel = this.getTopChannel()
	val otherChannel = other.getTopChannel()

	if (thisChannel is CategoryBehavior) {
		if (otherChannel is CategoryBehavior) {
			// Check based on category positions.
			return thisChannel.getPosition() > otherChannel.getPosition()
		}

		if (otherChannel is CategorizableChannelBehavior) {
			val otherCategory = otherChannel.asChannelOf<CategorizableChannel>().category
				?: return false // The other channel is at the top, outside a category.

			if (thisChannel.id == otherCategory.id) {
				return true // The other channel is within this category.
			}

			// Check based on category positions.
			return thisChannel.getPosition() > otherCategory.getPosition()
		}

		// Comparison doesn't make sense.
		error("Positional comparison between $this and $other doesn't make sense.")
	}

	if (otherChannel is CategoryBehavior) {
		if (thisChannel is CategorizableChannelBehavior) {
			val thisCategory = thisChannel.asChannelOf<CategorizableChannel>().category
				?: return true // This channel is at the top, outside a category.

			if (thisCategory.id == otherChannel.id) {
				return false // This channel is within the other category.
			}

			// Check based on category positions.
			return thisCategory.getPosition() > otherChannel.getPosition()
		}

		// Comparison doesn't make sense.
		error("Positional comparison between $this and $other doesn't make sense.")
	}

	if (thisChannel is CategorizableChannelBehavior && otherChannel is CategorizableChannelBehavior) {
		val thisCategory = thisChannel.asChannelOf<CategorizableChannel>().category
		val otherCategory = otherChannel.asChannelOf<CategorizableChannel>().category

		if (thisCategory?.id == otherCategory?.id) {
			// Both channels are at the top or in the same category, compare by position.
			return thisChannel.getPosition() > otherChannel.getPosition()
		}

		thisCategory ?: return true // This channel is at the top, but the other one isn't.
		otherCategory ?: return false // This channel isn't at the top, but the other one is.

		// Check based on category positions.
		return thisCategory.getPosition() > otherCategory.getPosition()
	}

	// Comparison doesn't make sense.
	error("Positional comparison between $this and $other doesn't make sense.")
}

/**
 * Check whether the receiver ([this]) is below the given [other] channel in the channel list.
 *
 * This function attempts to calculate this the same way the Discord client does:
 *
 * * Channels outside a category are always at the top.
 * * Compare channels within the same category (or both at the top) by position.
 * * Compare parent categories by position.
 *
 * This function throws an exception if the comparison doesn't make sense.
 * This shouldn't ever happen unless Kord's type system breaks.
 */
public suspend fun GuildChannelBehavior.isBelow(other: GuildChannelBehavior): Boolean {
	val thisChannel = this.getTopChannel()
	val otherChannel = other.getTopChannel()

	if (thisChannel is CategoryBehavior) {
		if (otherChannel is CategoryBehavior) {
			// Check based on category positions.
			return thisChannel.getPosition() < otherChannel.getPosition()
		}

		if (otherChannel is CategorizableChannelBehavior) {
			val otherCategory = otherChannel.asChannelOf<CategorizableChannel>().category
				?: return true // The other channel is at the top, outside a category.

			if (thisChannel.id == otherCategory.id) {
				return false // The other channel is within this category.
			}

			// Check based on category positions.
			return thisChannel.getPosition() < otherCategory.getPosition()
		}

		// Comparison doesn't make sense.
		error("Positional comparison between $this and $other doesn't make sense.")
	}

	if (otherChannel is CategoryBehavior) {
		if (thisChannel is CategorizableChannelBehavior) {
			val thisCategory = thisChannel.asChannelOf<CategorizableChannel>().category
				?: return false // This channel is at the top, outside a category.

			if (thisCategory.id == otherChannel.id) {
				return true // This channel is within the other category.
			}

			// Check based on category positions.
			return thisCategory.getPosition() < otherChannel.getPosition()
		}

		// Comparison doesn't make sense.
		error("Positional comparison between $this and $other doesn't make sense.")
	}

	if (thisChannel is CategorizableChannelBehavior && otherChannel is CategorizableChannelBehavior) {
		val thisCategory = thisChannel.asChannelOf<CategorizableChannel>().category
		val otherCategory = otherChannel.asChannelOf<CategorizableChannel>().category

		if (thisCategory?.id == otherCategory?.id) {
			// Both channels are at the top or in the same category, compare by position.
			return thisChannel.getPosition() < otherChannel.getPosition()
		}

		thisCategory ?: return false // This channel is at the top, but the other one isn't.
		otherCategory ?: return true // This channel isn't at the top, but the other one is.

		// Check based on category positions.
		return thisCategory.getPosition() < otherCategory.getPosition()
	}

	// Comparison doesn't make sense.
	error("Positional comparison between $this and $other doesn't make sense.")
}

// endregion
