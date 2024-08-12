/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.websockets

import dev.kordex.core.utils.runSuspended
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

public typealias Callback = suspend (Frame) -> Unit

public abstract class Websocket(public val session: DefaultWebSocketServerSession) {
	public lateinit var builder: WebsocketBuilder
		private set

	public lateinit var path: String
		private set

	public lateinit var registry: WebsocketRegistry
		private set

	private val logger = KotlinLogging.logger { }

	public abstract suspend fun handle(frame: Frame)

	public open suspend fun setup(call: ApplicationCall): Boolean =
		true

	public suspend fun close(reason: CloseReason = CloseReason(CloseReason.Codes.NORMAL, "")) {
		registry.removeSocket(this)
		session.close(reason)
	}

	public suspend fun send(frame: Frame) {
		session.send(frame)
	}

	public suspend fun send(content: String) {
		session.send(content)
	}

	public suspend fun send(content: ByteArray) {
		session.send(content)
	}

	public suspend inline fun <reified T> sendSerialized(data: T) {
		session.sendSerialized(data)
	}

	@Suppress("TooGenericExceptionCaught")
	internal suspend fun setupSocket(registry: WebsocketRegistry, builder: WebsocketBuilder, path: String) {
		this.builder = builder
		this.path = path
		this.registry = registry

		runSuspended {
			launch {
				while (session.isActive) {
					val frame = session.incoming.receive()

					try {
						handle(frame)
					} catch (e: Exception) {
						logger.error(e) { "Exception thrown in websocket handler" }
					}
				}
			}
		}
	}
}
