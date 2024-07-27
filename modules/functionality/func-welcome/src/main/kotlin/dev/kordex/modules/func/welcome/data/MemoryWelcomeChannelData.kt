/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
