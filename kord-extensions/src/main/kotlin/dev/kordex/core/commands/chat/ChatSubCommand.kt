/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.chat

import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.annotations.ExtensionDSL
import dev.kordex.core.commands.Arguments
import dev.kordex.core.extensions.Extension
import dev.kordex.core.utils.MutableStringKeyedMap
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
	public open val parent: ChatGroupCommand<out Arguments>,
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
			nameTranslationCache[locale] = name
				.withLocale(locale)
				.translate()
				.lowercase(locale)
		}

		return nameTranslationCache[locale]!!
	}
}
