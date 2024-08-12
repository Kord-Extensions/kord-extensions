/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.server

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
