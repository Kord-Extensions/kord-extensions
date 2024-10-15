/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
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

/**
 * Argument converter for Discord [Guild] arguments.
 *
 * This converter supports specifying guilds by supplying:
 * * A guild ID
 * * The name of the guild - the first matching guild available to the bot will be used
 * * `this` to refer to the current guild
 *
 * @see guild
 * @see guildList
 */
@Converter(
	"guild",

	types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE]
)
public class GuildConverter(
	override var validator: Validator<Guild> = null,
) : SingleConverter<Guild>() {
	override val signatureType: Key = CoreTranslations.Converters.Guild.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		if (arg.equals("this", true)) {
			val guild = context.getGuild()?.asGuildOrNull()

			if (guild != null) {
				this.parsed = guild

				return true
			}
		}

		this.parsed = findGuild(arg)
			?: throw DiscordRelayedException(
				CoreTranslations.Converters.Guild.Error.missing
					.withContext(context)
					.withOrdinalPlaceholders(arg)
			)

		return true
	}

	private suspend fun findGuild(arg: String): Guild? =
		try { // Try for a guild ID first
			val id = Snowflake(arg)

			kord.getGuildOrNull(id)
		} catch (_: NumberFormatException) { // It's not an ID, let's try the name
			kord.guilds.firstOrNull { it.name.equals(arg, true) }
		}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<StringChoiceBuilder> =
		wrapOption(arg.displayName, arg.description) {
			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false

		this.parsed = findGuild(optionValue)
			?: throw DiscordRelayedException(
				CoreTranslations.Converters.Guild.Error.missing
					.withContext(context)
					.withOrdinalPlaceholders(optionValue)
			)

		return true
	}
}
