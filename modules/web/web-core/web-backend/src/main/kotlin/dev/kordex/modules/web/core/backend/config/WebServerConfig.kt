/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.web.core.backend.config

import dev.kordex.core.utils.envOrNull

public class WebServerConfig {
	public var devMode: Boolean = envOrNull("WEB_DEV_MODE") != null

	@Suppress("MagicNumber")
	public var port: Int = 8080

	public lateinit var hostname: String
	public lateinit var siteTitle: String

	internal val oauth = OAuth()
	internal val reverseProxy = ReverseProxy()

	public fun oauth(body: WebServerConfig.OAuth.() -> Unit) {
		body(oauth)
	}

	public fun reverseProxy(body: WebServerConfig.ReverseProxy.() -> Unit) {
		body(reverseProxy)
	}

	public inner class OAuth {
		public lateinit var clientId: String
		public lateinit var clientSecret: String
	}

	public inner class ReverseProxy {
		public var headerMode: ForwardedHeaderMode =
			ForwardedHeaderMode.None
		public var headerStrategy: ForwardedHeaderStrategy =
			ForwardedHeaderStrategy.First
	}
}
