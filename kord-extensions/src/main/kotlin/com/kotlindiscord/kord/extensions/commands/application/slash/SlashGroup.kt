package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Slash command group, containing other slash commands.
 *
 * @param name Slash command group name
 * @param parent Parent slash command that this group belongs to
 */
public class SlashGroup(
    public val name: String,
    public val parent: SlashCommand<*, *>
) : KoinComponent {
    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /** @suppress **/
    public val logger: KLogger = KotlinLogging.logger {}

    /** List of subcommands belonging to this group. **/
    public val subCommands: MutableList<SlashCommand<*, *>> = mutableListOf()

    /** Command group description, which is required and shown on Discord. **/
    public lateinit var description: String

    /** Translation cache, so we don't have to look up translations every time. **/
    public val descriptionTranslationCache: MutableMap<Locale, String> = mutableMapOf()

    /** Return this group's description translated for the given locale, cached as required. **/
    public fun getTranslatedDescription(locale: Locale): String {
        // Only slash commands need this to be lower-cased.

        if (!descriptionTranslationCache.containsKey(locale)) {
            descriptionTranslationCache[locale] = translationsProvider.translate(
                this.description,
                this.parent.extension.bundle,
                locale
            ).lowercase()
        }

        return descriptionTranslationCache[locale]!!
    }

    /**
     * Validate this command group, ensuring it has everything it needs.
     *
     * Throws if not.
     */
    public fun validate() {
        if (!::description.isInitialized) {
            throw InvalidCommandException(name, "No group description given.")
        }

        if (subCommands.isEmpty()) {
            error("Command groups must contain at least one subcommand.")
        }
    }
}
