/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.extensions

import dev.kord.core.enableEvent
import dev.kord.core.event.Event
import dev.kord.gateway.Intents
import dev.kordex.core.EventHandlerRegistrationException
import dev.kordex.core.InvalidEventHandlerException
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.events.EventHandler
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * DSL function for easily registering an event handler.
 *
 * Use this in your setup function to register an event handler that reacts to a given event.
 *
 * @param body Builder lambda used for setting up the event handler object.
 */
@OptIn(InternalAPI::class)
public suspend inline fun <reified T : Event> Extension.event(
	noinline constructor: (Extension) -> EventHandler<T> = ::EventHandler,
	noinline body: suspend EventHandler<T>.() -> Unit,
): EventHandler<T> {
	val eventHandler = constructor(this)
	val logger = KotlinLogging.logger {}

	body.invoke(eventHandler)

	try {
		eventHandler.validate()
		eventHandler.type = T::class

		eventHandler.listenerRegistrationCallable = {
			eventHandler.job = bot.registerListenerForHandler(eventHandler)
		}

		bot.addEventHandler(eventHandler)
		eventHandlers.add(eventHandler)
	} catch (e: EventHandlerRegistrationException) {
		logger.error(e) { "Failed to register event handler - $e" }
	} catch (e: InvalidEventHandlerException) {
		logger.error(e) { "Failed to register event handler - $e" }
	}

	val fakeBuilder = Intents.Builder()

	fakeBuilder.enableEvent<T>()
	intents += fakeBuilder.build().values

	return eventHandler
}
