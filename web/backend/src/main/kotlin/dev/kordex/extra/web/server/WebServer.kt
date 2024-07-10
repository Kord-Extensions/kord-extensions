/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.server

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kordex.extra.web.WebRegistries
import dev.kordex.extra.web.config.WebServerConfig
import dev.kordex.extra.web.events.WebServerStartEvent
import dev.kordex.extra.web.events.WebServerStopEvent
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

public class WebServer(internal val config: WebServerConfig) : KordExKoinComponent {
	private val bot: ExtensibleBot by inject()

	public val registries: WebRegistries = WebRegistries()

	public var running: Boolean = false
		private set

	private val server = embeddedServer(Netty, port = config.port) {
		configureAuth(this)
		configureContentNegotiation(this)
		configureCORS(this)
		configureForwardedHeaders(this)

		// Required before routing
		configureWebSockets(this)

		configureRouting(this, config)
		configureStatusPages(this)
	}

	public suspend fun start() {
		bot.kordRef.launch {
			server.start()
		}

		running = true

		bot.send(WebServerStartEvent(this))
	}

	public suspend fun stop() {
		registries.teardown()

		server.stop(
			gracePeriodMillis = 0,
			timeoutMillis = 0
		)

		running = false

		bot.send(WebServerStopEvent())
	}
}
