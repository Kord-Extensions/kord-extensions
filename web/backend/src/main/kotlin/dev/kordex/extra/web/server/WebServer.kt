/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.server

import dev.kordex.extra.web.config.ForwardedHeaderMode
import dev.kordex.extra.web.config.ForwardedHeaderStrategy
import dev.kordex.extra.web.config.WebServerConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.xml.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlin.time.Duration.Companion.seconds

private val CORS_SCHEMES = listOf("http", "https", "ws", "wss")

public class WebServer(private val config: WebServerConfig) {
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

			allowMethod(HttpMethod.Options)
			allowMethod(HttpMethod.Put)
			allowMethod(HttpMethod.Patch)
			allowMethod(HttpMethod.Delete)
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

		install(WebSockets) {
			// For some reason, ktor uses Java durations instead of Kotlin ones for the easy properties?
			pingPeriodMillis = 15.seconds.inWholeMilliseconds
			timeoutMillis = 15.seconds.inWholeMilliseconds
		}

		routing {
			// TODO: Routing, incl. websockets
			// TODO: Static files

			setup()
		}
	}

	public fun start() {
		server.start(wait = false)
	}

	public fun stop() {
		server.stop(
			gracePeriodMillis = 0,
			timeoutMillis = 0
		)
	}

	private fun Routing.setup() {
		TODO()
	}
}
