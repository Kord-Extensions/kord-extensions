package com.kotlindiscord.kord.extensions.events

import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.userFor
import dev.kord.core.event.Event
import io.sentry.Breadcrumb
import io.sentry.SentryLevel
import java.util.*

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
     * Given a translation key and optional bundle name, return the translation for the locale provided by the bot's
     * configured locale resolvers.
     */
    public suspend fun translate(
        key: String,
        bundleName: String? = null,
        replacements: Array<Any?> = arrayOf()
    ): String {
        if (event !is Event) {
            return eventHandler.extension.bot.translationsProvider.get(key, bundleName)
        }

        val eventObj = event as Event
        var locale: Locale? = null

        val guild = guildFor(eventObj)
        val channel = channelFor(eventObj)
        val user = userFor(eventObj)

        for (resolver in eventHandler.extension.bot.settings.i18nBuilder.localeResolvers) {
            val result = resolver(guild, channel, user)

            if (result != null) {
                locale = result
                break
            }
        }

        return if (locale != null) {
            eventHandler.extension.bot.translationsProvider.translate(key, locale, bundleName, replacements)
        } else {
            eventHandler.extension.bot.translationsProvider.translate(key, bundleName, replacements)
        }
    }

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
