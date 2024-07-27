/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.web.core.backend.websockets

import io.ktor.server.websocket.*

public typealias WebsocketBuilderFun = (session: DefaultWebSocketServerSession) -> Websocket

public data class WebsocketBuilder(
	public val extension: String,
	public val builder: WebsocketBuilderFun,
)
