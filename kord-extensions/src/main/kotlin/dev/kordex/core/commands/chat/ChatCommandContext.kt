/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(KordUnsafe::class, KordExperimental::class)

package dev.kordex.core.commands.chat

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.annotations.ExtensionDSL
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.extensions.base.HelpProvider
import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.pagination.MessageButtonPaginator
import dev.kordex.core.pagination.builders.PaginatorBuilder
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale
import dev.kordex.core.utils.respond
import dev.kordex.parser.StringParser

/**
 * Command context object representing the context given to chat commands.
 *
 * @property chatCommand Chat command object
 * @param parser String parser instance, if any - will be `null` if this isn't a chat command.
 * @property argString String containing the command's unparsed arguments, raw, fresh from Discord itself.
 */
@ExtensionDSL
public open class ChatCommandContext<T : Arguments>(
	public val chatCommand: ChatCommand<out T>,
	eventObj: MessageCreateEvent,
	commandName: Key,
	public open val parser: StringParser,
	public val argString: String,
	cache: MutableStringKeyedMap<Any>,
) : CommandContext(chatCommand, eventObj, commandName, cache) {
	/** Event that triggered this command execution. **/
	public val event: MessageCreateEvent get() = eventObj as MessageCreateEvent

	/** Message channel this command happened in, if any. **/
	public open lateinit var channel: MessageChannelBehavior

	/** Guild this command happened in, if any. **/
	public open var guild: GuildBehavior? = null

	/** Guild member responsible for executing this command, if any. **/
	public open var member: MemberBehavior? = null

	/** User responsible for executing this command, if any (if `null`, it's a webhook). **/
	public open var user: UserBehavior? = null

	/** Message object containing this command invocation. **/
	public open lateinit var message: Message

	/** Arguments object containing this command's parsed arguments. **/
	public open lateinit var arguments: T

	override suspend fun populate() {
		channel = getChannel()
		guild = getGuild()
		member = getMember()
		user = getUser()

		message = getMessage()
	}

	/** @suppress Internal function **/
	public fun populateArgs(args: T) {
		arguments = args
	}

	override suspend fun getChannel(): MessageChannelBehavior = event.message.channel
	override suspend fun getGuild(): GuildBehavior? = event.guildId
		?.let { event.kord.unsafe.guild(it) }

	override suspend fun getMember(): MemberBehavior? = event.member
	override suspend fun getUser(): UserBehavior? = event.message.author

	/** Extract message information from event data, if that context is available. **/
	public open suspend fun getMessage(): Message = event.message

	/**
	 * Convenience function to create a button paginator using a builder DSL syntax. Handles the contextual stuff for
	 * you.
	 */
	public suspend fun paginator(
		defaultGroup: Key = EMPTY_KEY,

		pingInReply: Boolean = true,
		targetChannel: MessageChannelBehavior? = null,
		targetMessage: Message? = null,

		body: suspend PaginatorBuilder.() -> Unit,
	): MessageButtonPaginator {
		val builder = PaginatorBuilder(getLocale(), defaultGroup = defaultGroup)

		body(builder)

		return MessageButtonPaginator(pingInReply, targetChannel, targetMessage, builder)
	}

	/**
	 * Generate and send the help embed for this command, using the first loaded extensions that implements
	 * [HelpProvider].
	 *
	 * @return `true` if a help extension exists and help was sent, `false` otherwise.
	 */
	public suspend fun sendHelp(): Boolean {
		val helpExtension = this.command.extension.bot.findExtension<HelpProvider>() ?: return false
		val paginator = helpExtension.getCommandHelpPaginator(this, chatCommand)

		paginator.send()

		return true
	}

	/**
	 * Convenience function allowing for message responses with translated content.
	 */
	public suspend fun Message.respondTranslated(
		key: Key,
		placeholders: Array<Any?> = arrayOf(),
		useReply: Boolean = true,
	): Message = respond(
		key
			.withLocale(getLocale())
			.translateArray(placeholders),

		useReply
	)

	/**
	 * Convenience function allowing for message responses with translated content.
	 */
	public suspend fun Message.respondTranslated(
		key: Key,
		placeholders: Map<String, Any?>,
		useReply: Boolean = true,
	): Message = respond(
		key
			.withLocale(getLocale())
			.translateNamed(placeholders),

		useReply
	)
}
