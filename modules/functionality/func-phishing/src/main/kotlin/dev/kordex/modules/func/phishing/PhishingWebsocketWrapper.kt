/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("TooGenericExceptionCaught", "MagicNumber")

package dev.kordex.modules.func.phishing

import dev.kord.core.Kord
import dev.kordex.core.koin.KordExKoinComponent
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject

/**
 * Class wrapping the Sinking Yachts phishing API websocket. Mostly for lifecycle control, since we may need to
 * reconnect at times.
 *
 * @property appName Application name, passed in from the [PhishingApi]
 * @property callback Callback to invoke with domain changes
 */
class PhishingWebsocketWrapper(
	private val userAgent: String,
	private val callback: suspend (DomainChange) -> Unit,
) : KordExKoinComponent {
	private val logger = KotlinLogging.logger { }
	private var job: Job? = null

	private val kord: Kord by inject()

	private val client = HttpClient {
		install(ContentNegotiation) {
			json()
		}

		install(DefaultRequest) {
			header("User-Agent", userAgent)
			header("X-Identity", userAgent)
		}

		install(WebSockets)

		expectSuccess = true
	}

	/**
	 * Connect the websocket, and start processing incoming data. This will also stop any current websocket connection.
	 */
	suspend fun start() {
		stop()

		job = kord.launch {
			while (true) {
				try {
					websocket()
				} catch (e: ClosedReceiveChannelException) {
					logger.info { "Websocket closed by the server." }
				} catch (e: Exception) {
					logger.error(e) { "Exception thrown during webhook connection/processing." }
				}

				logger.info { "Reconnecting..." }

				delay(1000)
			}
		}
	}

	/** If the websocket is connected, disconnect by killing its job. **/
	fun stop() {
		job?.cancel()
		job = null
	}

	private suspend fun websocket() {
		client.webSocket(
			"wss://phish.sinking.yachts/feed",
		) {
			logger.info { "Websocket connected." }

			while (isActive) {
				val frame = incoming.receive() as Frame.Text
				val frameText = frame.readText()

				logger.debug { "Sinking Yachts <<< $frameText" }

				try {
					val change: DomainChange = Json.decodeFromString(frameText)

					callback(change)
				} catch (e: Exception) {
					logger.error(e) { "Failed to handle incoming domain change." }
				}
			}
		}
	}
}
