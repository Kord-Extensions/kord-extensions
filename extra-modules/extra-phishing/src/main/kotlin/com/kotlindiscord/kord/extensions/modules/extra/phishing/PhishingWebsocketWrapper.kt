/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("TooGenericExceptionCaught", "MagicNumber")

package com.kotlindiscord.kord.extensions.modules.extra.phishing

import dev.kord.core.Kord
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Class wrapping the Sinking Yachts phishing API websocket. Mostly for lifecycle control, since we may need to
 * reconnect at times.
 *
 * @property appName Application name, passed in from the [PhishingApi]
 * @property callback Callback to invoke with domain changes
 */
class PhishingWebsocketWrapper(
    private val appName: String,
    private val callback: suspend (DomainChange) -> Unit
) : KoinComponent {
    private val logger = KotlinLogging.logger { }
    private var job: Job? = null

    private val kord: Kord by inject()

    internal val client = HttpClient {
        install(ContentNegotiation)
        install(WebSockets)
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
            { header("X-Identity", "$appName (via Kord Extensions)") }
        ) {
            logger.info { "Websocket connected." }

            while (isActive) {
                val frame = incoming.receive() as Frame.Text
                val frameText = frame.readText()

                logger.debug { "Sinking Yachts <<< $frameText" }

                try {
                    val change = Json.decodeFromString<DomainChange>(frameText)

                    callback(change)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to handle incoming domain change." }
                }
            }
        }
    }
}
