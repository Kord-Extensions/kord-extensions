package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.userFor
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.event.Event
import io.sentry.Breadcrumb
import io.sentry.SentryLevel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Light wrapper class representing the context for a command's action.
 *
 * This is what `this` refers to in a command action body. You shouldn't have to
 * instantiate this yourself.
 *
 * @param command Respective command for this context object.
 * @param eventObj Event that triggered this command.
 * @param commandName MessageCommand name given by the user to invoke the command - lower-cased.
 * @param argsList Array of string arguments for this command.
 */
public abstract class CommandContext(
    public open val command: Command,
    public open val eventObj: Event,
    public open val commandName: String,
    public open val argsList: Array<String>
) : KoinComponent {
    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /** Sentry adapter, for easy access to Sentry functions. **/
    public val sentry: SentryAdapter by inject()

    /** A list of Sentry breadcrumbs created during command execution. **/
    public open val breadcrumbs: MutableList<Breadcrumb> = mutableListOf()

    /** Called before command processing, used to populate any extra variables from event data. **/
    public abstract suspend fun populate()

    /** Extract channel information from event data, if that context is available. **/
    public abstract suspend fun getChannel(): ChannelBehavior?

    /** Extract guild information from event data, if that context is available. **/
    public abstract suspend fun getGuild(): GuildBehavior?

    /** Extract member information from event data, if that context is available. **/
    public abstract suspend fun getMember(): MemberBehavior?

    /** Extract message information from event data, if that context is available. **/
    public abstract suspend fun getMessage(): MessageBehavior?

    /** Extract user information from event data, if that context is available. **/
    public abstract suspend fun getUser(): UserBehavior?

    /**
     * Add a Sentry breadcrumb to this command context.
     *
     * This should be used for the purposes of tracing what exactly is happening during your
     * command processing. If the bot administrator decides to enable Sentry integration, the
     * breadcrumbs will be sent to Sentry when there's a command processing error.
     */
    public fun breadcrumb(
        category: String? = null,
        level: SentryLevel? = null,
        message: String? = null,
        type: String? = null,

        data: Map<String, Any> = mapOf()
    ): Breadcrumb {
        val crumb = sentry.createBreadcrumb(category, level, message, type, data)

        breadcrumbs.add(crumb)

        return crumb
    }

    /** Resolve the locale for this command context. **/
    public suspend fun getLocale(): Locale {
        var locale: Locale? = null

        val guild = guildFor(eventObj)
        val channel = channelFor(eventObj)
        val user = userFor(eventObj)

        for (resolver in command.extension.bot.settings.i18nBuilder.localeResolvers) {
            val result = resolver(guild, channel, user)

            if (result != null) {
                locale = result
                break
            }
        }

        return locale ?: command.extension.bot.settings.i18nBuilder.defaultLocale
    }

    /**
     * Given a translation key and bundle name, return the translation for the locale provided by the bot's configured
     * locale resolvers.
     */
    public suspend fun translate(
        key: String,
        bundleName: String?,
        replacements: Array<Any?> = arrayOf()
    ): String {
        val locale = getLocale()

        return translationsProvider.translate(key, locale, bundleName, replacements)
    }

    /**
     * Given a translation key and possible replacements,return the translation for the given locale in the
     * extension's configured bundle, for the locale provided by the bot's configured locale resolvers.
     */
    public suspend fun translate(key: String, replacements: Array<Any?> = arrayOf()): String = translate(
        key,
        command.extension.bundle,
        replacements
    )
}
