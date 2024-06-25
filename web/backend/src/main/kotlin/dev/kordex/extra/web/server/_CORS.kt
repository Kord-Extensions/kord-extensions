/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

private val CORS_SCHEMES = listOf("http", "https", "ws", "wss")

public fun WebServer.configureCORS(app: Application) {
	app.install(CORS) {
		allowHost(config.hostname, schemes = CORS_SCHEMES)

		if (config.devMode) {
			allowHost("127.0.0.1", schemes = CORS_SCHEMES)
			allowHost("127.0.0.1:${config.port}", schemes = CORS_SCHEMES)

			allowHost("localhost", schemes = CORS_SCHEMES)
			allowHost("localhost:${config.port}", schemes = CORS_SCHEMES)
		}

		allowHeader(HttpHeaders.ContentType)

		allowMethod(HttpMethod.Delete)
		allowMethod(HttpMethod.Get)
		allowMethod(HttpMethod.Head)
		allowMethod(HttpMethod.Options)
		allowMethod(HttpMethod.Patch)
		allowMethod(HttpMethod.Post)
		allowMethod(HttpMethod.Put)
	}
}
