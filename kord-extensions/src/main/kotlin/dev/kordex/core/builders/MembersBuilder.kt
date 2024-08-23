/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders

import dev.kord.common.entity.Snowflake
import dev.kordex.core.annotations.BotBuilderDSL

/** Builder used for configuring the bot's member-related options. **/
@BotBuilderDSL
public class MembersBuilder {
	/** @suppress Internal list that shouldn't be modified by the user directly. **/
	public var guildsToFill: MutableList<Snowflake>? = mutableListOf()

	/**
	 * Whether to request the presences for the members that are requested from the guilds specified using the
	 * functions in this class.
	 *
	 * Requires the `GUILD_PRESENCES` privileged intent. Make sure you've enabled it for your bot!
	 */
	public var fillPresences: Boolean? = null

	/**
	 * Whether to lock when requesting members from guilds, preventing concurrent requests from being processed
	 * at once. This will slow down filling the cache with members, but may avoid hitting rate limits for larger
	 * bots.
	 */
	public var lockMemberRequests: Boolean = false

	/**
	 * Add a list of guild IDs to request members for.
	 *
	 * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
	 */
	@JvmName("fillSnowflakes")  // These are the same for the JVM
	public fun fill(ids: Collection<Snowflake>): Boolean? =
		guildsToFill?.addAll(ids)

	/**
	 * Add a list of guild IDs to request members for.
	 *
	 * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
	 */
	@JvmName("fillLongs")  // These are the same for the JVM
	public fun fill(ids: Collection<ULong>): Boolean? =
		guildsToFill?.addAll(ids.map { Snowflake(it) })

	/**
	 * Add a list of guild IDs to request members for.
	 *
	 * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
	 */
	@JvmName("fillStrings")  // These are the same for the JVM
	public fun fill(ids: Collection<String>): Boolean? =
		guildsToFill?.addAll(ids.map { Snowflake(it) })

	/**
	 * Add a guild ID to request members for.
	 *
	 * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
	 */
	public fun fill(id: Snowflake): Boolean? =
		guildsToFill?.add(id)

	/**
	 * Add a guild ID to request members for.
	 *
	 * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
	 */
	public fun fill(id: ULong): Boolean? =
		guildsToFill?.add(Snowflake(id))

	/**
	 * Add a guild ID to request members for.
	 *
	 * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
	 */
	public fun fill(id: String): Boolean? =
		guildsToFill?.add(Snowflake(id))

	/**
	 * Request members for all guilds the bot is on.
	 *
	 * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
	 */
	public fun all() {
		guildsToFill = null
	}

	/**
	 * Request no members from guilds at all. This is the default behaviour.
	 */
	public fun none() {
		guildsToFill = mutableListOf()
	}
}
