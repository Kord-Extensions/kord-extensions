package com.kotlindiscord.kord.extensions.commands.chat

import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.event.message.MessageCreateEvent
import java.util.*

/**
 * Class representing a subcommand.
 *
 * This is used for group commands, so that subcommands are aware of their parent.
 *
 * @param extension The [Extension] that registered this command.
 * @param parent The [ChatGroupCommand] this command exists under.
 */
@ExtensionDSL
public open class ChatSubCommand<T : Arguments>(
    extension: Extension,
    arguments: (() -> T)? = null,
    public open val parent: ChatGroupCommand<out Arguments>
) : ChatCommand<T>(extension, arguments) {

    override suspend fun runChecks(event: MessageCreateEvent, sendMessage: Boolean): Boolean {
        var result = parent.runChecks(event, sendMessage)

        if (result) {
            result = super.runChecks(event, sendMessage)
        }

        return result
    }

    /** Get the full command name, translated, with parent commands taken into account. **/
    public open suspend fun getFullTranslatedName(locale: Locale): String =
        parent.getFullTranslatedName(locale) + " " + this.getTranslatedName(locale)

    override fun getTranslatedName(locale: Locale): String {
        if (!nameTranslationCache.containsKey(locale)) {
            nameTranslationCache[locale] = translationsProvider.translate(
                this.name,
                this.extension.bundle,
                locale
            ).lowercase()
        }

        return nameTranslationCache[locale]!!
    }
}
