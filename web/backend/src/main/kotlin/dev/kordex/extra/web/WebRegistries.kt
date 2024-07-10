/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web

import dev.kordex.extra.web.pages.navigation.NavigationRegistry
import dev.kordex.extra.web.routes.RouteRegistry
import dev.kordex.extra.web.websockets.WebsocketRegistry

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
