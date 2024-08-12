/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.websockets

import dev.kordex.core.events.ExtensionStateEvent
import dev.kordex.core.extensions.ExtensionState
import dev.kordex.core.koin.KordExKoinComponent
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

public class WebsocketRegistry : KordExKoinComponent {
	private val socketBuilders: MutableMap<String, WebsocketBuilder> = mutableMapOf()
	private val activeSockets: MutableList<Websocket> = mutableListOf()

	public suspend fun handle(session: DefaultWebSocketServerSession) {
		val path = session.call.parameters.getAll("path")
			?.joinToString("/")

		val builder = socketBuilders[path]

		val socket = builder?.builder?.invoke(session)
			?: return session.call.respond(HttpStatusCode.NotFound)

		if (!socket.setup(session.call)) {
			if (
				session.call.response.status() == null &&
				!session.call.response.isSent &&
				!session.call.response.isCommitted
			) {
				session.call.respond(HttpStatusCode.Forbidden)
			}

			return
		}

		activeSockets.add(socket)
		socket.setupSocket(this, builder, path!!)
	}

	public suspend fun handleExtensionState(event: ExtensionStateEvent) {
		if (event.state == ExtensionState.UNLOADING) {
			socketBuilders
				.filter { it.value.extension == event.extension.name }
				.forEach { entry ->
					val builder = entry.value

					socketBuilders.remove(entry.key)

					activeSockets
						.filter { it.builder.extension == builder.extension }
						.forEach {
							it.close(CloseReason(CloseReason.Codes.NORMAL, "Extension was unloaded."))
						}
				}
		}
	}

	public fun add(path: String, builder: WebsocketBuilder): Boolean {
		if (path in socketBuilders) {
			return false
		}

		socketBuilders[path] = builder

		return true
	}

	public suspend fun remove(path: String): WebsocketBuilder? {
		activeSockets.filter { it.path == path }.forEach {
			it.close()
		}

		return socketBuilders.remove(path)
	}

	public fun removeSocket(socket: Websocket): Boolean =
		activeSockets.remove(socket)

	public suspend fun removeAll() {
		socketBuilders.clear()

		activeSockets.forEach { it.close() }
		activeSockets.clear()
	}
}
