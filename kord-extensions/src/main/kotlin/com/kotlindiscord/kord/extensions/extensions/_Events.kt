package com.kotlindiscord.kord.extensions.extensions

import com.kotlindiscord.kord.extensions.EventHandlerRegistrationException
import com.kotlindiscord.kord.extensions.InvalidEventHandlerException
import com.kotlindiscord.kord.extensions.events.EventHandler
import dev.kord.core.enableEvent
import dev.kord.core.event.Event
import dev.kord.gateway.Intents
import mu.KotlinLogging

/**
 * DSL function for easily registering an event handler.
 *
 * Use this in your setup function to register an event handler that reacts to a given event.
 *
 * @param body Builder lambda used for setting up the event handler object.
 */
public suspend inline fun <reified T : Event> Extension.event(
    noinline body: suspend EventHandler<T>.() -> Unit
): EventHandler<T> {
    val eventHandler = EventHandler<T>(this)
    val logger = KotlinLogging.logger {}

    body.invoke(eventHandler)

    try {
        eventHandler.validate()

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

    val fakeBuilder = Intents.IntentsBuilder()

    fakeBuilder.enableEvent<T>()
    intents += fakeBuilder.flags().values

    return eventHandler
}
