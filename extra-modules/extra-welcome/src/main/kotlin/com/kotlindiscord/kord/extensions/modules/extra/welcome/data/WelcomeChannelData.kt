/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.welcome.data

import dev.kord.common.entity.Snowflake

interface WelcomeChannelData {
	suspend fun getChannelURLs(): Map<Snowflake, String>
	suspend fun getUrlForChannel(channelId: Snowflake): String?

	suspend fun setUrlForChannel(channelId: Snowflake, url: String)
	suspend fun removeChannel(channelId: Snowflake): String?
}
