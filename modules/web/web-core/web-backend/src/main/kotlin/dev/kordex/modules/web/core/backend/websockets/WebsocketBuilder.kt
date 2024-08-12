/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.websockets

import io.ktor.server.websocket.*

public typealias WebsocketBuilderFun = (session: DefaultWebSocketServerSession) -> Websocket

public data class WebsocketBuilder(
	public val extension: String,
	public val builder: WebsocketBuilderFun,
)
