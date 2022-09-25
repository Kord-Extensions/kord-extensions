/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(KordUnsafe::class, KordExperimental::class)

package com.kotlindiscord.kord.extensions.components

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.userFor
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.sentry.SentryContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import io.sentry.Breadcrumb
import org.koin.core.component.inject
import java.util.*

/**
 * Abstract class representing the execution context for a generic components.
 *
 * @param E Event type the component makes use of
 * @param component Component object that's being interacted with
 * @param event Event that triggered this execution context
 * @param cache Data cache map shared with the defined checks.
 */
public abstract class ComponentContext<E : ComponentInteractionCreateEvent>(
    public open val component: Component,
    public open val event: E,
    public open val cache: MutableStringKeyedMap<Any>
) : KordExKoinComponent {
    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /** Configured bot settings object. **/
    public val settings: ExtensibleBotBuilder by inject()

    /** Current Sentry context, containing breadcrumbs and other goodies. **/
    public val sentry: SentryContext = SentryContext()

    /** Cached locale variable, stored and retrieved by [getLocale]. **/
    public open var resolvedLocale: Locale? = null

    /** Channel this component was interacted with within. **/
    public open lateinit var channel: MessageChannelBehavior

    /** Guild this component was interacted with within, if any. **/
    public open var guild: GuildBehavior? = null

    /** Member that interacted with this component, if on a guild. **/
    public open var member: MemberBehavior? = null

    /** The message the component is attached to. **/
    public open lateinit var message: Message

    /** User that interacted with this component. **/
    public open lateinit var user: UserBehavior

    /** Called before processing, used to populate any extra variables from event data. **/
    public open suspend fun populate() {
        channel = getChannel()
        guild = getGuild()
        member = getMember()
        message = getMessage()
        user = getUser()
    }

    /** Extract channel information from event data, if that context is available. **/
    @JvmName("getChannel1")
    public fun getChannel(): MessageChannelBehavior =
        event.interaction.channel

    /** Extract guild information from event data, if that context is available. **/
    @JvmName("getGuild1")
    public fun getGuild(): GuildBehavior? =
        event.interaction.data.guildId.value?.let { event.kord.unsafe.guild(it) }

    /** Extract member information from event data, if that context is available. **/
    @JvmName("getMember1")
    public fun getMember(): MemberBehavior? =
        getGuild()?.let { Member(event.interaction.data.member.value!!, event.interaction.user.data, event.kord) }

    /** Extract message information from event data, if that context is available. **/
    @JvmName("getMessage1")
    public fun getMessage(): Message =
        event.interaction.message

    /** Extract user information from event data, if that context is available. **/
    @JvmName("getUser1")
    public fun getUser(): UserBehavior =
        event.interaction.user

    /** Resolve the locale for this command context. **/
    public suspend fun getLocale(): Locale {
        var locale: Locale? = resolvedLocale

        if (locale != null) {
            return locale
        }

        val guild = guildFor(event)
        val channel = channelFor(event)
        val user = userFor(event)

        for (resolver in settings.i18nBuilder.localeResolvers) {
            val result = resolver(guild, channel, user, event.interaction)

            if (result != null) {
                locale = result
                break
            }
        }

        resolvedLocale = locale ?: settings.i18nBuilder.defaultLocale

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
     * Given a translation key and possible replacements, return the translation for the given locale in the
     * component's configured bundle, for the locale provided by the bot's configured locale resolvers.
     */
    public suspend fun translate(key: String, replacements: Array<Any?> = arrayOf()): String = translate(
        key,
        component.bundle,
        replacements
    )

    /**
     * Given a translation key and bundle name, return the translation for the locale provided by the bot's configured
     * locale resolvers.
     */
    public suspend fun translate(
        key: String,
        bundleName: String?,
        replacements: Map<String, Any?>
    ): String {
        val locale = getLocale()

        return translationsProvider.translate(key, locale, bundleName, replacements)
    }

    /**
     * Given a translation key and possible replacements, return the translation for the given locale in the
     * component's configured bundle, for the locale provided by the bot's configured locale resolvers.
     */
    public suspend fun translate(key: String, replacements: Map<String, Any?>): String = translate(
        key,
        component.bundle,
        replacements
    )

    /**
     * @param breadcrumb breadcrumb data will be modified to add the component context information
     */
    public suspend fun addContextDataToBreadcrumb(breadcrumb: Breadcrumb) {
        val channel = channel.asChannelOrNull()
        val guild = guild?.asGuildOrNull()
        val message = message

        if (channel != null) {
            breadcrumb.data["channel"] = when (channel) {
                is DmChannel -> "Private Message (${channel.id})"
                is GuildMessageChannel -> "#${channel.name} (${channel.id})"

                else -> channel.id.toString()
            }
        }

        if (guild != null) {
            breadcrumb.data["guild"] = "${guild.name} (${guild.id})"
        }

        breadcrumb.data["message"] = message.id.toString()
    }
}
