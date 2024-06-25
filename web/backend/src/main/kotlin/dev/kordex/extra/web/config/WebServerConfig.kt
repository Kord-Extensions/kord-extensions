/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.config

import com.kotlindiscord.kord.extensions.utils.envOrNull

public class WebServerConfig {
	public var devMode: Boolean = envOrNull("WEB_DEV_MODE") != null

	@Suppress("MagicNumber")
	public var port: Int = 8080

	public var forwardedHeaderMode: ForwardedHeaderMode = ForwardedHeaderMode.None
	public var forwardedHeaderStrategy: ForwardedHeaderStrategy = ForwardedHeaderStrategy.First

	public lateinit var hostname: String

	internal val oauth = OAuth()

	public fun oauth(body: OAuth.() -> Unit) {
		body(oauth)
	}

	public inner class OAuth {
		public lateinit var clientId: String
		public lateinit var clientSecret: String
	}
}
