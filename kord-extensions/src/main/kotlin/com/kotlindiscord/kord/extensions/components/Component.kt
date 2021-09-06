package com.kotlindiscord.kord.extensions.components

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandRegistry
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import dev.kord.core.Kord
import dev.kord.rest.builder.component.ActionRowBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Abstract class representing a basic Discord component. **/
public abstract class Component : KoinComponent {
    /** Component width, how many "slots" in one row it needs to be added to the row. **/
    public open val unitWidth: Int = 1

    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /** Quick access to the command registry. **/
    public val registry: ApplicationCommandRegistry by inject()

    /** Bot settings object. **/
    public val settings: ExtensibleBotBuilder by inject()

    /** Bot object. **/
    public val bot: ExtensibleBot by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public val kord: Kord by inject()

    /** Sentry adapter, for easy access to Sentry functions. **/
    public val sentry: SentryAdapter by inject()

    /** Translation bundle, to retrieve translations from. **/
    public open var bundle: String? = null

    /** Validation function, called to ensure the component is valid, throws exceptions if not. **/
    public abstract fun validate()

    /** Called to apply the given component to a Kord [ActionRowBuilder]. **/
    public abstract fun apply(builder: ActionRowBuilder)
}
