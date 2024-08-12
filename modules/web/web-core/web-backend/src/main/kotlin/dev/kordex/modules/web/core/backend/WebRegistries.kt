/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend

import dev.kordex.modules.web.core.backend.pages.navigation.NavigationRegistry
import dev.kordex.modules.web.core.backend.routes.RouteRegistry
import dev.kordex.modules.web.core.backend.websockets.WebsocketRegistry

public class WebRegistries {
	public lateinit var navigation: NavigationRegistry
		private set

	public lateinit var routes: RouteRegistry
		private set

	public lateinit var websockets: WebsocketRegistry
		private set

	public fun setup() {
		if (!this::navigation.isInitialized) {
			navigation = NavigationRegistry()
		}

		if (!this::routes.isInitialized) {
			routes = RouteRegistry()
		}

		if (!this::websockets.isInitialized) {
			websockets = WebsocketRegistry()
		}
	}

	public suspend fun teardown() {
		if (this::navigation.isInitialized) {
			navigation.removeAll()
		}

		if (this::routes.isInitialized) {
			routes.removeAll()
		}

		if (this::websockets.isInitialized) {
			websockets.removeAll()
		}
	}
}
