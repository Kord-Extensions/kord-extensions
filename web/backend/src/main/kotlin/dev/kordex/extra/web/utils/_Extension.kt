/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.utils

import com.kotlindiscord.kord.extensions.events.EventContext
import dev.kordex.extra.web.events.WebServerStartEvent
import dev.kordex.extra.web.pages.navigation.ExtensionNavigation
import dev.kordex.extra.web.routes.Route
import dev.kordex.extra.web.types.Identifier
import dev.kordex.extra.web.websockets.WebsocketBuilder
import dev.kordex.extra.web.websockets.WebsocketBuilderFun

public fun EventContext<WebServerStartEvent>.apiRoute(route: Route) {
	if (!event.server.registries.routes.add(route)) {
		error("Route at ${route.path} for extension ${eventHandler.extension.name} already exists.")
	}
}

public fun EventContext<WebServerStartEvent>.websocket(path: String, body: WebsocketBuilderFun) {
	val socketBuilder = WebsocketBuilder(eventHandler.extension.name, body)

	if (!event.server.registries.websockets.add(path, socketBuilder)) {
		error("Websocket at $path for extension ${eventHandler.extension.name} already exists.")
	}
}

public fun EventContext<WebServerStartEvent>.navigation(icon: Identifier, setup: ExtensionNavigation.() -> Unit) {
	val navigation = ExtensionNavigation(eventHandler.extension.name, icon, setup)

	navigation.setup()

	// TODO: Register!
}
