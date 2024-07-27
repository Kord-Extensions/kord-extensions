/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Emoji
import dev.kord.core.entity.StandardEmoji
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.i18n.DEFAULT_KORDEX_BUNDLE
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
	override val signatureTypeString: String = "converters.emoji.signatureType"
	override val bundle: String = DEFAULT_KORDEX_BUNDLE

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		val emoji: Emoji = findEmoji(arg, context)
			?: throw DiscordRelayedException(
				context.translate("converters.emoji.error.missing", replacements = arrayOf(arg))
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
			} catch (e: NumberFormatException) {
				throw DiscordRelayedException(
					context.translate("converters.emoji.error.invalid", replacements = arrayOf(id))
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
			} catch (e: NumberFormatException) {  // Not an ID, let's check names
				val currentResult = kord.guilds.mapNotNull {
					it.emojis.firstOrNull { emojiObj -> emojiObj.name?.lowercase().equals(name, true) }
				}.firstOrNull()

				currentResult ?: EmojiManager.getByDiscordAlias(arg).get().unicode.let {
					StandardEmoji(it)
				}
			}
		}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false

		val emoji: Emoji = findEmoji(optionValue, context)
			?: throw DiscordRelayedException(
				context.translate("converters.emoji.error.missing", replacements = arrayOf(optionValue))
			)

		parsed = emoji
		return true
	}
}
