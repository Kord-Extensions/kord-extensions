/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.routes

import com.kotlindiscord.kord.extensions.events.ExtensionStateEvent
import com.kotlindiscord.kord.extensions.extensions.ExtensionState
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kordex.extra.web.Route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

public class RouteRegistry : KordExKoinComponent {
	private val routes: MutableMap<String, Route> = mutableMapOf()

	public suspend fun handle(verb: Verb, context: PipelineContext<Unit, ApplicationCall>) {
		val call = context.call

		val path = call.parameters.getAll("path")
			?.joinToString("/")

		val route = routes[path]
			?: return call.respond(HttpStatusCode.NotFound)

		when (verb) {
			Verb.DELETE -> route.delete(call)
			Verb.GET -> route.get(call)
			Verb.HEAD -> route.head(call)
			Verb.OPTIONS -> route.options(call)
			Verb.PATCH -> route.patch(call)
			Verb.POST -> route.post(call)
			Verb.PUT -> route.put(call)
		}
	}

	public fun handleExtensionState(event: ExtensionStateEvent) {
		if (event.state == ExtensionState.UNLOADING) {
			val toRemove = routes.filter { it.value.extension == event.extension.name }

			toRemove.forEach { routes.remove(it.key) }
		}
	}

	public fun add(route: Route): Boolean {
		val path = "${route.extension}/${route.path}"

		if (path in routes) {
			return false
		}

		routes[path] = route

		return true
	}

	public fun remove(route: Route): Route? {
		val path = "${route.extension}/${route.path}"

		return routes.remove(path)
	}

	public fun removeAll() {
		routes.clear()
	}
}
