/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(
	ExperimentalCoroutinesApi::class,
)

package dev.kordex.core.commands.converters.impl

import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.interaction.ChannelOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChannelBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.OptionWrapper
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.commands.wrapOption
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
import dev.kordex.core.utils.translate
import dev.kordex.parser.StringParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.toList

/**
 * Argument converter for Discord [Channel] arguments.
 *
 * This converter supports specifying channels by supplying:
 *
 * * A channel mention
 * * A channel ID, with or without a `#` prefix
 * * A channel name, with or without a `#` prefix (the required guild will be searched for the first matching channel)
 * * `this` to refer to the current channel
 *
 * @param requireSameGuild Whether to require that the channel passed is on the same guild as the message.
 * @param requiredGuild Lambda returning a specific guild to require the channel to be in, if needed.
 */
@Converter(
	"channel",

	types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],

	imports = ["dev.kord.common.entity.ChannelType", "dev.kord.common.entity.Snowflake"],

	builderExtraStatements = [
		"/** Add a channel type to the set of types the given channel must match. **/",
		"public fun requireChannelType(type: ChannelType) {",
		"    requiredChannelTypes.add(type)",
		"}"
	],

	builderFields = [
		"public var requireSameGuild: Boolean = true",
		"public var requiredGuild: (suspend () -> Snowflake)? = null",
		"public var requiredChannelTypes: MutableSet<ChannelType> = mutableSetOf()",
	],
)
public class ChannelConverter(
	private val requireSameGuild: Boolean = true,
	private var requiredGuild: (suspend () -> Snowflake)? = null,
	private val requiredChannelTypes: Set<ChannelType> = setOf(),
	override var validator: Validator<Channel> = null,
) : SingleConverter<Channel>() {
	override val signatureType: Key = CoreTranslations.Converters.Channel.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		if (arg.equals("this", true)) {
			val channel = context.getChannel().asChannelOrNull()

			if (channel != null) {
				this.parsed = channel

				return true
			}
		}

		val channel: Channel = findChannel(arg, context) ?: throw DiscordRelayedException(
			CoreTranslations.Converters.Channel.Error.missing
				.withContext(context)
				.withOrdinalPlaceholders(arg)
		)

		parsed = channel
		return true
	}

	private suspend fun findChannel(arg: String, context: CommandContext): Channel? {
		val channel: Channel? = if (arg.startsWith("<#") && arg.endsWith(">")) { // Channel mention
			val id = arg.substring(2, arg.length - 1)

			try {
				kord.getChannel(Snowflake(id.toLong()))
			} catch (_: NumberFormatException) {
				throw DiscordRelayedException(
					CoreTranslations.Converters.Channel.Error.invalid
						.withContext(context)
						.withOrdinalPlaceholders(id)
				)
			}
		} else {
			val string: String = if (arg.startsWith("#")) arg.substring(1) else arg

			try {
				kord.getChannel(Snowflake(string.toLong()))
			} catch (_: NumberFormatException) { // It's not a numeric ID, so let's try a channel name
				kord.guilds
					.flatMapConcat { it.channels }
					.filter { it.name.equals(string, true) }
					.filter {
						if (requiredChannelTypes.isNotEmpty()) {
							it.type in requiredChannelTypes
						} else {
							true
						}
					}
					.toList()
					.firstOrNull()
			}
		}

		channel ?: return null

		if (requiredGuild != null || requireSameGuild) {
			if (channel is GuildChannel) {
				val requiredGuildId: Snowflake = requiredGuild?.invoke()
					?: context.getGuild()?.id
					?: return null  // Edge case, which may happen if cache-only resolution doesn't return a guild.

				if (channel.guildId != requiredGuildId) {
					return null  // Channel isn't in the right guild
				}
			} else {
				return null  // Prevent DM channel ID probing
			}
		}

		if (requiredChannelTypes.isNotEmpty() && channel.type !in requiredChannelTypes) {
			val locale = context.getLocale()

			throw DiscordRelayedException(
				CoreTranslations.Converters.Channel.Error.wrongType
					.withContext(context)
					.withOrdinalPlaceholders(
						channel.type,
						requiredChannelTypes.joinToString { "**${it.translate(locale)}**" }
					)
			)
		}

		return channel
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<ChannelBuilder> =
		wrapOption(arg.displayName, arg.description) {
			channelTypes = requiredChannelTypes.toList()

			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = if (context.eventObj is AutoCompleteInteractionCreateEvent) {
			val id = (option as? ChannelOptionValue)?.value ?: return false
			kord.getChannel(id) ?: return false
		} else {
			(option as? ChannelOptionValue)?.resolvedObject ?: return false
		}

		this.parsed = optionValue

		return true
	}
}
