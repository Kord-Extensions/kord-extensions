/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.server

import dev.kordex.core.ExtensibleBot
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.modules.web.core.backend.WebRegistries
import dev.kordex.modules.web.core.backend.config.WebServerConfig
import dev.kordex.modules.web.core.backend.events.WebServerStartEvent
import dev.kordex.modules.web.core.backend.events.WebServerStopEvent
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
