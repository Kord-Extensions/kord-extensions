/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.chat

import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
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
    public override val parent: ChatGroupCommand<out Arguments>,
) : ChatCommand<T>(extension, arguments) {

    override suspend fun runChecks(
        event: MessageCreateEvent,
        sendMessage: Boolean,
        cache: MutableStringKeyedMap<Any>,
    ): Boolean =
        parent.runChecks(event, sendMessage, cache) &&
            super.runChecks(event, sendMessage, cache)

    /** Get the full command name, translated, with parent commands taken into account. **/
    public open suspend fun getFullTranslatedName(locale: Locale): String =
        parent.getFullTranslatedName(locale) + " " + this.getTranslatedName(locale)

    override fun getTranslatedName(locale: Locale): String {
        if (!nameTranslationCache.containsKey(locale)) {
            nameTranslationCache[locale] = translationsProvider.translate(
                this.name,
                this.resolvedBundle,
                locale
            ).lowercase()
        }

        return nameTranslationCache[locale]!!
    }
}
