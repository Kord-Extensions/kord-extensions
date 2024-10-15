/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.routes

import dev.kordex.core.events.ExtensionStateEvent
import dev.kordex.core.extensions.ExtensionState
import dev.kordex.core.koin.KordExKoinComponent
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.RoutingContext
import io.ktor.util.pipeline.*

public class RouteRegistry : KordExKoinComponent {
	private val routes: MutableMap<String, Route> = mutableMapOf()

	public suspend fun handle(verb: Verb, context: RoutingContext) {
		val call = context.call

		val path = call.parameters.getAll("path")
			?.joinToString("/")

		val route = routes[path]
			?: return call.respond(HttpStatusCode.NotFound)

		if (route.beforeRequest(verb, call)) {
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
