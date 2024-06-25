/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.server

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
