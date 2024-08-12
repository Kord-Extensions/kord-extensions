/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.welcome.data

import dev.kord.common.entity.Snowflake

interface WelcomeChannelData {
	suspend fun getChannelURLs(): Map<Snowflake, String>
	suspend fun getUrlForChannel(channelId: Snowflake): String?

	suspend fun setUrlForChannel(channelId: Snowflake, url: String)
	suspend fun removeChannel(channelId: Snowflake): String?
}
