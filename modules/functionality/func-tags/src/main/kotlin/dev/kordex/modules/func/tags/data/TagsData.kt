/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.tags.data

import dev.kord.common.entity.Snowflake

/**
 * Interface representing data storage for the tags extension. Extend this and implement the functions to create your
 * own storage setup for your bot.
 *
 * For all functions that take a guild ID (except for the `delete` and `set` functions), you should also always match
 * tags with a guild ID of `null`. These are global tags, and should be accessible regardless of the guild context.
 *
 * All functions are suspending to allow for database access, for example, where needed.
 */
interface TagsData {
	/**
	 * Get a tag by tag key and guild ID.
	 */
	suspend fun getTagByKey(key: String, guildId: Snowflake? = null): Tag?

	/**
	 * Get a list of tags with the given category and guild ID.
	 */
	suspend fun getTagsByCategory(category: String, guildId: Snowflake? = null): List<Tag>

	/**
	 * Get a list of tags with the given partial key and guild ID. Should find tags with keys that partially match
	 * the given [partialKey], but it's up to you whether to use `contains` or `startsWith`.
	 */
	suspend fun getTagsByPartialKey(partialKey: String, guildId: Snowflake? = null): List<Tag>

	/**
	 * Get a list of tags with the given partial title and guild ID. Should find tags with titles that partially match
	 * the given [partialTitle], but it's up to you whether to use `contains` or `startsWith`.
	 */
	suspend fun getTagsByPartialTitle(partialTitle: String, guildId: Snowflake? = null): List<Tag>

	/**
	 * Get a set of all potential tag categories.
	 */
	suspend fun getAllCategories(guildId: Snowflake? = null): Set<String>

	/**
	 * Find tags using optionally-provided criteria. Ignore null values, so their criteria is always matched.
	 */
	suspend fun findTags(category: String? = null, guildId: Snowflake? = null, key: String? = null): List<Tag>

	/**
	 * Given a [Tag] object, store it (and persist it if needed), overwriting any tags with the same key and guild ID.
	 */
	suspend fun setTag(tag: Tag)

	/**
	 * Convenience function wrapping [deleteTagByKey].
	 */
	suspend fun deleteTag(tag: Tag): Tag? = deleteTagByKey(tag.key, tag.guildId)

	/**
	 * Delete a tag by tag key and guild ID, if it exists. Return `null` if the tag didn't exist, otherwise return
	 * the removed tag.
	 */
	suspend fun deleteTagByKey(key: String, guildId: Snowflake? = null): Tag?
}
