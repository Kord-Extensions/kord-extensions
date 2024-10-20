/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.chat

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.CommandRegistrationException
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.commands.Arguments
import dev.kordex.core.extensions.Extension
import dev.kordex.core.i18n.SupportedLocales
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.utils.getLocale
import dev.kordex.parser.StringParser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.inject

/**
 * A class for the registration and dispatching of message-based commands.
 */
public open class ChatCommandRegistry : KordExKoinComponent {
	private val logger = KotlinLogging.logger { }

	/** Current instance of the bot. **/
	public val bot: ExtensibleBot by inject()

	/** Kord instance, backing the ExtensibleBot. **/
	public val kord: Kord by inject()

	/** Chat command parser object. **/
	public open val parser: ChatCommandParser = ChatCommandParser()

	/**
	 * A list of all registered commands.
	 */
	public open val commands: MutableList<ChatCommand<out Arguments>> = mutableListOf()

	/** @suppress **/
	public val botSettings: ExtensibleBotBuilder by inject()

	/** Whether chat commands are enabled in the bot's settings. **/
	public open val enabled: Boolean get() = botSettings.chatCommandsBuilder.enabled

	/**
	 * Directly register a [ChatCommand] to this command registry.
	 *
	 * Generally speaking, you shouldn't call this directly - instead, create an [Extension] and
	 * call the [ChatGroupCommand.chatCommand] function in your [Extension.setup] function.
	 *
	 * This function will throw a [CommandRegistrationException] if the command has already been registered, if
	 * a command with the same name exists, or if a command with one of the same aliases exists.
	 *
	 * @param command The command to be registered.
	 * @throws CommandRegistrationException Thrown if the command could not be registered.
	 */
	@Throws(CommandRegistrationException::class)
	public open fun add(command: ChatCommand<out Arguments>) {
		val existingCommand = commands.any { it.name == command.name }

		if (existingCommand) {
			throw CommandRegistrationException(
				command.name,
				"Chat command registered using duplicate name: ${command.name}"
			)
		}

		if (commands.contains(command)) {
			throw CommandRegistrationException(
				command.name,
				"Chat command registered twice: ${command.name}"
			)
		}

		val commandAliases = SupportedLocales.ALL_LOCALES_SET.flatMap {
			command.getTranslatedAliases(it)
		}

		val existingAliases = commands.flatMap { c ->
			SupportedLocales.ALL_LOCALES_SET.flatMap {
				c.getTranslatedAliases(it)
			}
		}

		val matchingAliases = commandAliases.intersect(existingAliases)

		if (matchingAliases.isNotEmpty()) {
			logger.warn {
				"Chat command named using ${command.name} provides aliases used by other " +
					"commands: ${matchingAliases.joinToString()}"
			}
		}

		commands.add(command)
	}

	/**
	 * Directly remove a registered [ChatCommand] from this command registry.
	 *
	 * This function is used when extensions are unloaded, in order to clear out their commands.
	 * No exception is thrown if the command wasn't registered.
	 *
	 * @param command The command to be removed.
	 */
	public open fun remove(command: ChatCommand<out Arguments>): Boolean = commands.remove(command)

	/**
	 * Given a [MessageCreateEvent], return the prefix that should be used for a command invocation.
	 *
	 * By default, this can be set in [ExtensibleBotBuilder] - but you can override this function in subclasses if
	 * needed.
	 */
	public open suspend fun getPrefix(event: MessageCreateEvent): String =
		botSettings.chatCommandsBuilder.prefixCallback(event, botSettings.chatCommandsBuilder.defaultPrefix)

	/**
	 * Check whether the given string starts with a mention referring to the bot. If so, the matching mention string
	 * is returned, otherwise `null`.
	 */
	public open fun String.startsWithSelfMention(): String? {
		val mention = "<@${kord.selfId.value}>"
		val nickMention = "<@!${kord.selfId.value}>"

		return when {
			startsWith(mention) -> mention
			startsWith(nickMention) -> nickMention

			else -> null
		}
	}

	/**
	 * Handles an incoming [MessageCreateEvent] and dispatches a command invocation, if possible.
	 */
	public open suspend fun handleEvent(event: MessageCreateEvent) {
		if (botSettings.chatCommandsBuilder.ignoreSelf && event.message.author?.id == bot.kordRef.selfId) {
			logger.trace { "Ignoring message ${event.message.id} as it was sent by us." }

			return
		}

		val message = event.message
		var commandName: String?
		val prefix = getPrefix(event)
		var content = message.content

		if (content.isEmpty()) {
			// Empty message.
			return
		}

		val mention = content.startsWithSelfMention()

		content = when {
			// Starts with the right mention and mentions are allowed, so remove it
			mention != null && botSettings.chatCommandsBuilder.invokeOnMention -> content.substring(mention.length)

			// Starts with the right prefix, so remove it
			content.startsWith(prefix) -> content.substring(prefix.length)

			// Not a valid command, so stop here
			else -> return
		}.trim()  // Remove unnecessary spaces

		commandName = content.split(" ").first()
		content = content.substring(commandName.length).trim()  // Remove the command name and extra whitespace

		commandName = commandName.lowercase()

		val command = getCommand(commandName, event)
		val parser = StringParser(content)

		command?.call(event, commandName, parser, content)
	}

	/**
	 * Given a command name and [MessageCreateEvent], try to find a matching command.
	 *
	 * If a command supports locale fallback, this will also attempt to resolve names via the bot's default locale.
	 */
	public open suspend fun getCommand(name: String, event: MessageCreateEvent): ChatCommand<out Arguments>? {
		val defaultLocale = botSettings.i18nBuilder.defaultLocale
		val locale = event.getLocale()

		return commands.firstOrNull { it.getTranslatedName(locale) == name }
			?: commands.firstOrNull { it.getTranslatedAliases(locale).contains(name) }
			?: commands.firstOrNull { it.localeFallback && it.getTranslatedName(defaultLocale) == name }
			?: commands.firstOrNull { it.localeFallback && it.getTranslatedAliases(defaultLocale).contains(name) }
	}
}
