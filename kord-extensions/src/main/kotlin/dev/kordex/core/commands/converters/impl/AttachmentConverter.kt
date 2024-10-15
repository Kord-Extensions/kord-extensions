/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.core.entity.Attachment
import dev.kord.core.entity.interaction.AttachmentOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.AttachmentBuilder
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

/**
 * Argument converter for Discord attachments.
 *
 * This converter can only be used in slash commands.
 */
@Converter(
	"attachment",

	types = [ConverterType.OPTIONAL, ConverterType.SINGLE],
)
public class AttachmentConverter(
	override var validator: Validator<Attachment> = null,
) : SingleConverter<Attachment>() {
	override val signatureType: Key = CoreTranslations.Converters.Attachment.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean =
		throw DiscordRelayedException(
			CoreTranslations.Converters.Attachment.Error.slashCommandsOnly
				.withContext(context)
		)

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<AttachmentBuilder> =
		wrapOption(arg.displayName, arg.description) {
			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? AttachmentOptionValue)?.resolvedObject ?: return false
		this.parsed = optionValue

		return true
	}
}
