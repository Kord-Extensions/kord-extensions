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
 * In-memory tags storage, intended for testing. Not optimised, doesn't store the tags when the bot stops. This class
 * uses `startsWith` to match tags based on partial keys and titles.
 *
 * Use your own implementation for your bots.
 */
class MemoryTagsData : TagsData {
	private val tags: MutableList<Tag> = mutableListOf()

	override suspend fun getTagByKey(key: String, guildId: Snowflake?): Tag? =
		tags.firstOrNull {
			it.key == key && (it.guildId == guildId || it.guildId == null)
		}

	override suspend fun getTagsByCategory(category: String, guildId: Snowflake?): List<Tag> =
		tags.filter {
			it.category == category && (it.guildId == guildId || it.guildId == null)
		}

	override suspend fun getTagsByPartialKey(partialKey: String, guildId: Snowflake?): List<Tag> =
		tags.filter {
			it.key.startsWith(partialKey, true) && (it.guildId == guildId || it.guildId == null)
		}

	override suspend fun getTagsByPartialTitle(partialTitle: String, guildId: Snowflake?): List<Tag> =
		tags.filter {
			it.title.startsWith(partialTitle, true) && (it.guildId == guildId || it.guildId == null)
		}

	override suspend fun getAllCategories(guildId: Snowflake?): Set<String> =
		tags.filter { it.guildId == guildId || it.guildId == null }
			.map { it.category }.toSet()

	override suspend fun findTags(category: String?, guildId: Snowflake?, key: String?): List<Tag> =
		tags.filter {
			(category == null || it.category.equals(category, true)) &&
				(guildId == null || it.guildId == guildId) &&
				(key == null || it.key.equals(key, true))
		}

	override suspend fun setTag(tag: Tag) {
		tags.removeIf {
			it.key == tag.key && it.guildId == tag.guildId
		}

		tags.add(tag)
	}

	override suspend fun deleteTagByKey(key: String, guildId: Snowflake?): Tag? {
		val tag = getTagByKey(key, guildId)

		tags.removeIf {
			it.key == key && it.guildId == guildId
		}

		return tag
	}
}
