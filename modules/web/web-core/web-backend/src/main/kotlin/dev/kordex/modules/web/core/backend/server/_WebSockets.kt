/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.server

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

public fun WebServer.configureWebSockets(app: Application) {
	app.install(WebSockets) {
		// For some reason, ktor uses Java durations instead of Kotlin ones for the easy properties?
		pingPeriodMillis = 15.seconds.inWholeMilliseconds
		timeoutMillis = 15.seconds.inWholeMilliseconds

		contentConverter = KotlinxWebsocketSerializationConverter(Json.Default)
	}
}
