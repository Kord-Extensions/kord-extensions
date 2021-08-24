package com.kotlindiscord.kord.extensions.commands.application

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.userFor
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.sentry.SentryContext
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.interaction.ApplicationInteractionCreateEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Base class representing the shared functionality for an application command's context.
 *
 * @param genericEvent Generic event object to populate data from.
 * @param genericCommand Generic command object that this context belongs to.
 */
public abstract class ApplicationCommandContext(
    public val genericEvent: ApplicationInteractionCreateEvent,
    public val genericCommand: ApplicationCommand<*>
) : KoinComponent {
    /** Current bot setting object. **/
    public val botSettings: ExtensibleBotBuilder by inject()

    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /** Current Sentry context, containing breadcrumbs and other goodies. **/
    public val sentry: SentryContext = SentryContext()

    /** Cached locale variable, stored and retrieved by [getLocale]. **/
    public open var resolvedLocale: Locale? = null

    /** Channel this command was executed within. **/
    public open lateinit var channel: MessageChannelBehavior

    /** Guild this command was executed within, if any. **/
    public open var guild: GuildBehavior? = null

    /** Member that executed this command, if on a guild. **/
    public open var member: MemberBehavior? = null

    /** User that executed this command. **/
    public open lateinit var user: UserBehavior

    /** Called before processing, used to populate any extra variables from event data. **/
    public open suspend fun populate() {
        // NOTE: This must always be alphabetical, some latter calls rely on earlier ones

        channel = getChannel()
        guild = getGuild()
        member = getMember()
        user = getUser()
    }

    /** Extract channel information from event data, if that context is available. **/
    public open suspend fun getChannel(): MessageChannelBehavior =
        genericEvent.interaction.getChannel()

    /** Extract guild information from event data, if that context is available. **/
    public open suspend fun getGuild(): GuildBehavior? =
        (channel as? GuildMessageChannel)?.guild

    /** Extract member information from event data, if that context is available. **/
    public open suspend fun getMember(): MemberBehavior? =
        guild?.getMember(genericEvent.interaction.user.id)

    /** Extract user information from event data, if that context is available. **/
    public open suspend fun getUser(): UserBehavior =
        genericEvent.interaction.user

    /** Resolve the locale for this command context. **/
    public suspend fun getLocale(): Locale {
        var locale: Locale? = resolvedLocale

        if (locale != null) {
            return locale
        }

        val guild = guildFor(genericEvent)
        val channel = channelFor(genericEvent)
        val user = userFor(genericEvent)

        for (resolver in botSettings.i18nBuilder.localeResolvers) {
            val result = resolver(guild, channel, user)

            if (result != null) {
                locale = result
                break
            }
        }

        resolvedLocale = locale ?: botSettings.i18nBuilder.defaultLocale

        return resolvedLocale!!
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
        genericCommand.extension.bundle,
        replacements
    )
}
