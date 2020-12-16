package com.kotlindiscord.kord.extensions.events

import io.sentry.Breadcrumb
import io.sentry.SentryLevel

/**
 * Light wrapper representing the context for an event handler's action.
 *
 * This is what `this` refers to in an event handler action body. You shouldn't need to instantiate this yourself.
 *
 * @param eventHandler Respective event handler for this context object.
 * @param event Event that triggered this event handler.
 */
public open class EventContext<T : Any>(
    public open val eventHandler: EventHandler<T>,
    public open val event: T
) {
    /** A list of Sentry breadcrumbs created during event processing. **/
    public open val breadcrumbs: MutableList<Breadcrumb> = mutableListOf()

    /**
     * Add a Sentry breadcrumb to this event context.
     *
     * This should be used for the purposes of tracing what exactly is happening during your
     * event processing. If the bot administrator decides to enable Sentry integration, the
     * breadcrumbs will be sent to Sentry when there's an event processing error.
     */
    public fun breadcrumb(
        category: String? = null,
        level: SentryLevel? = null,
        message: String? = null,
        type: String? = null,

        data: Map<String, Any> = mapOf()
    ): Breadcrumb {
        val crumb = eventHandler.extension.bot.sentry.createBreadcrumb(category, level, message, type, data)

        breadcrumbs.add(crumb)

        return crumb
    }
}
