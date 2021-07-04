package com.kotlindiscord.kord.extensions.components.contexts

import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.InteractionFollowup
import dev.kord.core.entity.interaction.PublicFollowupMessage
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.interaction.EphemeralFollowupMessageCreateBuilder
import dev.kord.rest.builder.interaction.PublicFollowupMessageCreateBuilder
import io.sentry.Breadcrumb
import io.sentry.SentryLevel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Context object representing the execution context of an actionable component interaction.
 *
 * @property extension Extension object this interaction happened within.
 * @property event Event that was fired for this interaction.
 * @property components Components container this button belongs to.
 * @property interactionResponse Interaction response, if automatically acked.
 * @property interaction Convenience access to the properly-typed interaction object.
 */
@OptIn(KordPreview::class)
@ExtensionDSL
public abstract class ActionableComponentContext<T : ComponentInteraction>(
    public open val extension: Extension,
    public open val event: InteractionCreateEvent,
    public open val components: Components,
    public open var interactionResponse: InteractionResponseBehavior? = null,
    public open val interaction: T = event.interaction as T
) : KoinComponent {
    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /** Sentry adapter, for easy access to Sentry functions. **/
    public val sentry: SentryAdapter by inject()

    /** A list of Sentry breadcrumbs created during interaction execution. **/
    public open val breadcrumbs: MutableList<Breadcrumb> = mutableListOf()

    /** Cached locale variable, stored and retrieved by [getLocale]. **/
    public open var resolvedLocale: Locale? = null

    /** Whether a response or ack has already been sent by the user. **/
    public open val acked: Boolean get() = interactionResponse != null

    /** Channel this interaction happened in. **/
    public open lateinit var channel: MessageChannel

    /** Guild this interaction happened in. **/
    public open var guild: Guild? = null

    /** Guild member responsible for executing this interaction. **/
    public open var member: MemberBehavior? = null

    /** User responsible for executing this interaction. **/
    public open lateinit var user: UserBehavior

    /** Whether we're working ephemerally, or null if no ack or response was sent yet. **/
    public open val isEphemeral: Boolean?
        get() = when (interactionResponse) {
            is EphemeralInteractionResponseBehavior -> true
            is PublicInteractionResponseBehavior -> false

            else -> null
        }

    /** Called before processing, used to populate any extra variables from event data. **/
    public suspend fun populate() {
        channel = getChannel()
        guild = getGuild()
        member = getMember()
        user = getUser()
    }

    /** Extract channel information from event data, if that context is available. **/
    public suspend fun getChannel(): MessageChannel = channelFor(event)!!.asChannel() as MessageChannel

    /** Extract guild information from event data, if that context is available. **/
    public suspend fun getGuild(): Guild? = guildFor(event)?.asGuildOrNull()

    /** Extract member information from event data, if that context is available. **/
    public suspend fun getMember(): MemberBehavior? = memberFor(event)?.asMemberOrNull()

    /** Extract user information from event data, if that context is available. **/
    public suspend fun getUser(): UserBehavior = event.interaction.user

    /**
     * Send an acknowledgement manually, assuming you have `autoAck` set to `NONE`.
     *
     * Note that what you supply for `ephemeral` will decide how the rest of your interactions - both responses and
     * follow-ups. They must match in ephemeral state.
     *
     * This function will throw an exception if an acknowledgement or response has already been sent.
     *
     * @param ephemeral Whether this should be an ephemeral acknowledgement or not.
     */
    public suspend fun ack(ephemeral: Boolean): InteractionResponseBehavior {
        if (acked) {
            error("Attempted to acknowledge an interaction that's already been acknowledged.")
        }

        interactionResponse = if (ephemeral) {
            event.interaction.acknowledgeEphemeral()
        } else {
            event.interaction.acknowledgePublic()
        }

        return interactionResponse!!
    }

    /**
     * Assuming an acknowledgement or response has been sent, send an ephemeral follow-up message.
     *
     * This function will throw an exception if no acknowledgement or response has been sent yet, or this interaction
     * has already been interacted with in a non-ephemeral manner.
     *
     * Note that ephemeral follow-ups require a content string, and may not contain embeds or files.
     */
    public suspend inline fun ephemeralFollowUp(
        builder: EphemeralFollowupMessageCreateBuilder.() -> Unit = {}
    ): InteractionFollowup {
        if (interactionResponse == null) {
            error("Tried send an interaction follow-up before acknowledging it.")
        }

        if (isEphemeral == false) {
            error("Tried send an ephemeral follow-up for a public interaction.")
        }

        return (interactionResponse as EphemeralInteractionResponseBehavior).followUpEphemeral(builder)
    }

    /**
     * Assuming an acknowledgement or response has been sent, send a public follow-up message.
     *
     * This function will throw an exception if no acknowledgement or response has been sent yet, or this interaction
     * has already been interacted with in an ephemeral manner.
     */
    public suspend inline fun publicFollowUp(
        builder: PublicFollowupMessageCreateBuilder.() -> Unit
    ): PublicFollowupMessage {
        if (interactionResponse == null) {
            error("Tried send an interaction follow-up before acknowledging it.")
        }

        if (isEphemeral == true) {
            error("Tried to send a public follow-up for an ephemeral interaction.")
        }

        return (interactionResponse as PublicInteractionResponseBehavior).followUp(builder)
    }

    /**
     * Add a Sentry breadcrumb to this context.
     *
     * This should be used for the purposes of tracing what exactly is happening during your
     * interaction processing. If the bot administrator decides to enable Sentry integration, the
     * breadcrumbs will be sent to Sentry when there's an interaction processing error.
     */
    public fun breadcrumb(
        category: String? = null,
        level: SentryLevel? = null,
        type: String? = null,

        data: Map<String, Any> = mapOf()
    ): Breadcrumb {
        val crumb = sentry.createBreadcrumb(category, level, null, type, data)

        breadcrumbs.add(crumb)

        return crumb
    }

    /** Resolve the locale for this context. **/
    public suspend fun getLocale(): Locale {
        var locale: Locale? = resolvedLocale

        if (locale != null) {
            return locale
        }

        for (resolver in extension.bot.settings.i18nBuilder.localeResolvers) {
            val result = resolver(guild, channel, user)

            if (result != null) {
                locale = result
                break
            }
        }

        resolvedLocale = locale ?: extension.bot.settings.i18nBuilder.defaultLocale

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
        extension.bundle,
        replacements
    )

    /** Convenience function to send a response follow-up message containing only text. **/
    public suspend fun respond(text: String): Any = when (isEphemeral) {
        true -> ephemeralFollowUp { content = text }
        false -> publicFollowUp { content = text }

        else -> interactionResponse = interaction.respondEphemeral { content = text }
    }
}
