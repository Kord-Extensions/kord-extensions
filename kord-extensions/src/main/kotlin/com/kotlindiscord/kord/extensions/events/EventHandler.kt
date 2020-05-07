package com.kotlindiscord.kord.extensions.events

import com.gitlab.kordlib.core.event.Event
import com.kotlindiscord.kord.extensions.InvalidEventHandlerException
import com.kotlindiscord.kord.extensions.extensions.Extension
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Class representing an event handler. Event handlers react to a given Kord event.
 *
 * You shouldn't need to use this class directly - instead, create an [Extension] and use the
 * [event function][Extension.event] to register your event handler, by overriding the [Extension.setup]
 * function.
 *
 * @param extension The [Extension] that registered this event handler.
 */
class EventHandler<T : Event>(val extension: Extension) {
    /**
     * @suppress
     */
    lateinit var body: suspend EventHandler<T>.(T) -> Unit

    /**
     * @suppress
     */
    val checkList: MutableList<suspend T.() -> Boolean> = mutableListOf()

    /**
     * An internal function used to ensure that all of an event handler's required arguments are present.
     *
     * @throws InvalidEventHandlerException Thrown when a required argument hasn't been set.
     */
    @Throws(InvalidEventHandlerException::class)
    fun validate() {
        if (!::body.isInitialized) {
            throw InvalidEventHandlerException("No event handler action given.")
        }
    }

    // region: DSL functions

    /**
     * Define what will happen when your event handler is invoked.
     *
     * @param action The body of your event handler, which will be executed when it is invoked.
     */
    fun action(action: suspend EventHandler<T>.(T) -> Unit) {
        // TODO: Documented @samples
        this.body = action
    }

    /**
     * Define a check which must pass for the event handler to be executed.
     *
     * A event handler may have multiple checks - all checks must pass for the event handler to be executed.
     * Checks will be run in the order that they're defined.
     *
     * This function can be used DSL-style with a given body, or it can be passed one or more
     * predefined functions. See the samples for more information.
     *
     * @param checks Checks to apply to this event handler.
     */
    fun check(vararg checks: suspend T.() -> Boolean) {
        // TODO: Documented @samples
        checks.forEach { checkList.add(it) }
    }

    // endregion

    /**
     * Execute this event handler, given an event.
     *
     * This function takes an event of type T and executes the [event handler body][action], assuming all checks pass.
     *
     * If an exception is thrown by the [event handler body][action], it is caught and a traceback
     * is printed.
     *
     * @param event The given event object.
     */
    suspend fun call(event: T) {
        for (check in checkList) {
            if (!check.invoke(event)) {
                return
            }
        }

        @Suppress("TooGenericExceptionCaught")  // Anything could happen here
        try {
            this.body(this, event)
        } catch (e: Exception) {
            logger.error(e) { "Error during execution of event handler ($event)" }
        }
    }
}
