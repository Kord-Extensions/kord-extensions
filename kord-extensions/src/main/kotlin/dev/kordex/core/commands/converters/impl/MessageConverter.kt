/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.core.exception.EntityNotFoundException
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.OptionWrapper
import dev.kordex.core.commands.chat.ChatCommandContext
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.commands.wrapOption
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
import dev.kordex.parser.StringParser
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Argument converter for discord [Message] arguments.
 *
 * This converter supports specifying messages by supplying:
 * * A Discord message jump link
 * * A message ID (it will be assumed that the message is in the current channel).
 *
 * @param requireGuild Whether to require messages to be in a specified guild.
 * @param requiredGuild Lambda returning a specific guild to require the member to be in. If omitted, defaults to the
 * guild the command was invoked in.
 * @param useReply Whether to use the replied-to message (if there is one) instead of trying to parse an argument.
 */
@Converter(
	"message",

	types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
	imports = ["dev.kord.common.entity.Snowflake"],

	builderFields = [
		"public var requireGuild: Boolean = false",
		"public var requiredGuild: (suspend () -> Snowflake)? = null",
		"public var useReply: Boolean = true",
	]
)
public class MessageConverter(
	private var requireGuild: Boolean = false,
	private var requiredGuild: (suspend () -> Snowflake)? = null,
	private var useReply: Boolean = true,
	override var validator: Validator<Message> = null,
) : SingleConverter<Message>() {
	override val signatureType: Key = CoreTranslations.Converters.Message.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		if (useReply && context is ChatCommandContext<*>) {
			val messageReference = context.message.asMessage().messageReference

			if (messageReference != null) {
				val message = messageReference.message?.asMessageOrNull()

				if (message != null) {
					parsed = message
					return true
				}
			}
		}

		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		parsed = findMessage(arg, context)

		return true
	}

	private suspend fun findMessage(arg: String, context: CommandContext): Message {
		val requiredGid: Snowflake? = if (requiredGuild != null) {
			requiredGuild!!.invoke()
		} else {
			context.getGuild()?.id
		}

		return if (arg.startsWith("https://")) { // It's a message URL
			@Suppress("MagicNumber")
			val split: List<String> = arg.substring(8).split("/").takeLast(3)

			@Suppress("MagicNumber")
			if (split.size < 3) {
				throw DiscordRelayedException(
					CoreTranslations.Converters.Message.Error.invalidUrl
						.withContext(context)
						.withOrdinalPlaceholders(arg)
				)
			}

			@Suppress("MagicNumber")
			val gid: Snowflake = try {
				Snowflake(split[0])
			} catch (_: NumberFormatException) {
				throw DiscordRelayedException(
					CoreTranslations.Converters.Message.Error.invalidGuildId
						.withContext(context)
						.withOrdinalPlaceholders(split[0])
				)
			}

			if (requireGuild && requiredGid != gid) {
				logger.trace { "Matching guild ($requiredGid) required, but guild ($gid) doesn't match." }

				errorNoMessage(arg, context)
			}

			@Suppress("MagicNumber")
			val cid: Snowflake = try {
				Snowflake(split[1])
			} catch (_: NumberFormatException) {
				throw DiscordRelayedException(
					CoreTranslations.Converters.Message.Error.invalidChannelId
						.withContext(context)
						.withOrdinalPlaceholders(split[1])
				)
			}

			val channel: GuildChannel? = kord.getGuildOrNull(gid)?.getChannel(cid)

			if (channel == null) {
				logger.trace { "Unable to find channel ($cid) for guild ($gid)." }

				errorNoMessage(arg, context)
			}

			if (channel !is GuildMessageChannel) {
				logger.trace { "Specified channel ($cid) is not a guild message channel." }

				errorNoMessage(arg, context)
			}

			@Suppress("MagicNumber")
			val mid: Snowflake = try {
				Snowflake(split[2])
			} catch (_: NumberFormatException) {
				throw DiscordRelayedException(
					CoreTranslations.Converters.Message.Error.invalidMessageId
						.withContext(context)
						.withOrdinalPlaceholders(split[2])
				)
			}

			try {
				channel.getMessage(mid)
			} catch (_: EntityNotFoundException) {
				errorNoMessage(mid.toString(), context)
			}
		} else { // Try a message ID
			val channel: ChannelBehavior = context.getChannel()

			if (channel !is GuildMessageChannel && channel !is DmChannel) {
				logger.trace { "Current channel is not a guild message channel or DM channel." }

				errorNoMessage(arg, context)
			}

			@Suppress("USELESS_IS_CHECK")  // This may change as Discord updates their channel types.
			if (channel !is MessageChannel) {
				logger.trace { "Current channel is not a message channel, so it can't contain messages." }

				errorNoMessage(arg, context)
			}

			try {
				channel.getMessage(Snowflake(arg))
			} catch (_: NumberFormatException) {
				throw DiscordRelayedException(
					CoreTranslations.Converters.Message.Error.invalidMessageId
						.withContext(context)
						.withOrdinalPlaceholders(arg)
				)
			} catch (_: EntityNotFoundException) {
				errorNoMessage(arg, context)
			}
		}
	}

	private suspend fun errorNoMessage(arg: String, context: CommandContext): Nothing {
		throw DiscordRelayedException(
			CoreTranslations.Converters.Message.Error.missing
				.withContext(context)
				.withOrdinalPlaceholders(arg)
		)
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<StringChoiceBuilder> =
		wrapOption(arg.displayName, arg.description) {
			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false

		parsed = findMessage(optionValue, context)

		return true
	}
}
