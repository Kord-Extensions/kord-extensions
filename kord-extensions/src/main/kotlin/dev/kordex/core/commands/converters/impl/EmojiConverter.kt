/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Emoji
import dev.kord.core.entity.StandardEmoji
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.StringChoiceBuilder
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
import dev.kordex.parser.StringParser
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import net.fellbaum.jemoji.EmojiManager

/**
 * Argument converter for [Emoji] arguments.
 *
 * This converter supports specifying emojis by supplying:
 *
 * * The actual emoji itself, unicode or custom.
 * * The custom emoji ID, either with or without surrounding colons.
 * * The custom emoji name, either with or without surrounding colons,
 *   using the first matching emoji available to the bot.
 * * The Unicode emoji name, as used by Discord.
 *
 * **Note:** For custom/guild emojis, your bot must have the `GuildEmojis` intent enabled!
 *
 * @see emoji
 * @see emojiList
 */
@Converter(
	"emoji",

	types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE]
)
public class EmojiConverter(
	override var validator: Validator<Emoji> = null,
) : SingleConverter<Emoji>() {
	override val signatureType: Key = CoreTranslations.Converters.Emoji.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		val emoji: Emoji = findEmoji(arg, context)
			?: throw DiscordRelayedException(
				CoreTranslations.Converters.Emoji.Error.missing
					.withContext(context)
					.withOrdinalPlaceholders(arg)
			)

		parsed = emoji

		return true
	}

	private suspend fun findEmoji(arg: String, context: CommandContext): Emoji? =
		if (EmojiManager.isEmoji(arg)) {  // Unicode emoji
			StandardEmoji(arg)
		} else if (arg.startsWith("<a:") || arg.startsWith("<:") && arg.endsWith('>')) { // Emoji mention
			val id: String = arg.substring(0, arg.length - 1).split(":").last()

			try {
				val snowflake = Snowflake(id)

				kord.guilds.mapNotNull {
					it.getEmojiOrNull(snowflake)
				}.firstOrNull()
			} catch (_: NumberFormatException) {
				throw DiscordRelayedException(
					CoreTranslations.Converters.Emoji.Error.invalid
						.withContext(context)
						.withOrdinalPlaceholders(id)
				)
			}
		} else { // ID or name
			val name = if (arg.startsWith(":") && arg.endsWith(":")) {
				arg.substring(1, arg.length - 1)
			} else {
				arg
			}

			try {
				val snowflake = Snowflake(name)

				kord.guilds.mapNotNull {
					it.getEmojiOrNull(snowflake)
				}.firstOrNull()
			} catch (_: NumberFormatException) {  // Not an ID, let's check names
				val currentResult = kord.guilds.mapNotNull {
					it.emojis.firstOrNull { emojiObj -> emojiObj.name?.lowercase().equals(name, true) }
				}.firstOrNull()

				currentResult ?: EmojiManager.getByDiscordAlias(arg).get().unicode.let {
					StandardEmoji(it)
				}
			}
		}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<StringChoiceBuilder> =
		wrapOption(arg.displayName, arg.description) {
			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false

		val emoji: Emoji = findEmoji(optionValue, context)
			?: throw DiscordRelayedException(
				CoreTranslations.Converters.Emoji.Error.missing
					.withContext(context)
					.withOrdinalPlaceholders(optionValue)
			)

		parsed = emoji
		return true
	}
}
