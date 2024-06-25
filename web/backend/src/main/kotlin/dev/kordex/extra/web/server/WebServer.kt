/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.server

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kordex.extra.web.config.WebServerConfig
import dev.kordex.extra.web.events.WebServerStartEvent
import dev.kordex.extra.web.events.WebServerStopEvent
import dev.kordex.extra.web.routes.RouteRegistry
import dev.kordex.extra.web.websockets.WebsocketRegistry
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.core.component.inject

public class WebServer(internal val config: WebServerConfig) : KordExKoinComponent {
	private val bot: ExtensibleBot by inject()

	public val routeRegistry: RouteRegistry = RouteRegistry()
	public val wsRegistry: WebsocketRegistry = WebsocketRegistry()

	public var running: Boolean = false
		private set

	private val server = embeddedServer(Netty, port = config.port) {
		configureAuth(this)
		configureContentNegotiation(this)
		configureCORS(this)
		configureForwardedHeaders(this)
		configureRouting(this)
		configureStatusPages(this)
		configureWebSockets(this)
	}

	public suspend fun start() {
		server.start(wait = false)

		running = true

		bot.send(WebServerStartEvent(this))
	}

	public suspend fun stop() {
		routeRegistry.removeAll()
		wsRegistry.removeAll()

		server.stop(
			gracePeriodMillis = 0,
			timeoutMillis = 0
		)

		running = false

		bot.send(WebServerStopEvent())
	}
}
