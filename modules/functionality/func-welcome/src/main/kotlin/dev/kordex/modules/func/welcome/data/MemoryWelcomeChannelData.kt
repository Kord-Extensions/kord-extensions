/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.welcome.data

import dev.kord.common.entity.Snowflake

class MemoryWelcomeChannelData : WelcomeChannelData {
	private val data: MutableMap<Snowflake, String> = mutableMapOf()

	override suspend fun getChannelURLs(): Map<Snowflake, String> = data
	override suspend fun getUrlForChannel(channelId: Snowflake): String? = data[channelId]
	override suspend fun removeChannel(channelId: Snowflake): String? = data.remove(channelId)

	override suspend fun setUrlForChannel(channelId: Snowflake, url: String) {
		data[channelId] = url
	}
}
