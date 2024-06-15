/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web

import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kordex.extra.web.config.WebServerConfig
import dev.kordex.extra.web.server.WebServer

public class WebExtension(private val config: WebServerConfig) : Extension() {
	override val name: String = "kordex-web"

	public lateinit var server: WebServer

	override suspend fun setup() {
		server = WebServer(config)

		server.start()
	}

	override suspend fun unload() {
		if (this::server.isInitialized) {
			server.stop()
		}
	}
}
