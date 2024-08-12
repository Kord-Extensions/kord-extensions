/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.tags.config

import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kordex.core.checks.types.Check
import dev.kordex.modules.func.tags.TagFormatter

/**
 * Interface representing the configuration for the tags module. Extend this and register an instance with Koin to
 * change how the module is configured.
 *
 * All functions are suspending to allow for database access, for example, where needed.
 */
interface TagsConfig {
	/**
	 * Get the configured tag formatter callback, used to turn a tag into a message. **Users configuring this to avoid
	 * creating embeds should make sure to append to the message content instead of replacing it.**
	 */
	suspend fun getTagFormatter(): TagFormatter

	/**
	 * Get the configured user command checks, used to ensure a user-facing command can be run.
	 */
	suspend fun getUserCommandChecks(): List<Check<*>>

	/**
	 * Get the configured staff command checks, used to ensure a staff-facing command can be run.
	 */
	suspend fun getStaffCommandChecks(): List<Check<*>>

	/**
	 * Get the logging channel for logging tag updates, returning `null` if this isn't needed.
	 */
	suspend fun getLoggingChannelOrNull(guild: Guild): GuildMessageChannel?

	/**
	 * Function wrapping [getLoggingChannelOrNull], with a non-null assertion.
	 */
	suspend fun getLoggingChannel(guild: Guild): GuildMessageChannel =
		getLoggingChannelOrNull(guild)!!
}
