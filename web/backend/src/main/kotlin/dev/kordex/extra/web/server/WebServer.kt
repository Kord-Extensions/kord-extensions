/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.server

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kordex.extra.web.config.ForwardedHeaderMode
import dev.kordex.extra.web.config.ForwardedHeaderStrategy
import dev.kordex.extra.web.config.WebServerConfig
import dev.kordex.extra.web.events.WebServerStartEvent
import dev.kordex.extra.web.events.WebServerStopEvent
import dev.kordex.extra.web.routes.RouteRegistry
import dev.kordex.extra.web.routes.Verb
import dev.kordex.extra.web.websockets.WebsocketRegistry
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.xml.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

private val CORS_SCHEMES = listOf("http", "https", "ws", "wss")

public class WebServer(private val config: WebServerConfig) : KordExKoinComponent {
	private val bot: ExtensibleBot by inject()

	public val routeRegistry: RouteRegistry = RouteRegistry()
	public val wsRegistry: WebsocketRegistry = WebsocketRegistry()

	public var running: Boolean = false
		private set

	private val server = embeddedServer(Netty, port = config.port) {
		install(ContentNegotiation) {
			json()
			xml()
		}

		install(CORS) {
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

		when (config.forwardedHeaderMode) {
			ForwardedHeaderMode.None -> {}

			ForwardedHeaderMode.Forwarded -> install(ForwardedHeaders) {
				when (val s = config.forwardedHeaderStrategy) {
					ForwardedHeaderStrategy.First -> useFirstValue()
					ForwardedHeaderStrategy.Last -> useLastValue()

					is ForwardedHeaderStrategy.SkipKnown -> skipKnownProxies(s.known)
					is ForwardedHeaderStrategy.SkipLast -> skipLastProxies(s.number)

					is ForwardedHeaderStrategy.Custom -> extractValue(s.block)

					is ForwardedHeaderStrategy.XCustom -> error("Use the `Custom` strategy in `Forwarded` mode.")
				}
			}

			ForwardedHeaderMode.XForwarded -> install(XForwardedHeaders) {
				when (val s = config.forwardedHeaderStrategy) {
					ForwardedHeaderStrategy.First -> useFirstProxy()
					ForwardedHeaderStrategy.Last -> useLastProxy()

					is ForwardedHeaderStrategy.SkipKnown -> skipKnownProxies(s.known)
					is ForwardedHeaderStrategy.SkipLast -> skipLastProxies(s.number)

					is ForwardedHeaderStrategy.XCustom -> extractEdgeProxy(s.block)

					is ForwardedHeaderStrategy.Custom -> error("Use the `XCustom` strategy in `XForwarded` mode.")
				}
			}
		}

		install(StatusPages) {
			// TODO: Error handling, etc
		}

		install(WebSockets) {
			// For some reason, ktor uses Java durations instead of Kotlin ones for the easy properties?
			pingPeriodMillis = 15.seconds.inWholeMilliseconds
			timeoutMillis = 15.seconds.inWholeMilliseconds

			contentConverter = KotlinxWebsocketSerializationConverter(Json.Default)
		}

		routing {
			// TODO: Routing, incl. websockets
			// TODO: Static files

			setup()
		}
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

	private fun Routing.setup() {
		if (config.devMode) {
			get("/") {
				call.respondRedirect("http://localhost:5173")
			}
		} else {
			singlePageApplication {
				useResources = true
				filesPath = "dev/kordex/extra/web/frontend"
			}
		}

		route("/api/extensions/{path...}") {
			delete {
				routeRegistry.handle(Verb.DELETE, this)
			}

			get {
				routeRegistry.handle(Verb.GET, this)
			}

			head {
				routeRegistry.handle(Verb.HEAD, this)
			}

			options {
				routeRegistry.handle(Verb.OPTIONS, this)
			}

			patch {
				routeRegistry.handle(Verb.PATCH, this)
			}

			post {
				routeRegistry.handle(Verb.POST, this)
			}

			put {
				routeRegistry.handle(Verb.PUT, this)
			}
		}

		route("/ws/extensions/{path...}") {
			webSocket {
				wsRegistry.handle(this)
			}
		}

		// TODO: Other routes
	}
}
