/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.utils

import dev.kordex.core.events.EventContext
import dev.kordex.modules.web.core.backend.events.WebServerStartEvent
import dev.kordex.modules.web.core.backend.pages.navigation.ExtensionNavigation
import dev.kordex.modules.web.core.backend.routes.Route
import dev.kordex.modules.web.core.backend.types.Identifier
import dev.kordex.modules.web.core.backend.websockets.WebsocketBuilder
import dev.kordex.modules.web.core.backend.websockets.WebsocketBuilderFun

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
