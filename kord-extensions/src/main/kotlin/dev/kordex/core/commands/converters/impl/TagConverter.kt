/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.impl

import com.ibm.icu.util.ULocale
import dev.kord.common.entity.ForumTag
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.ForumChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
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
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.utils.getLocale
import dev.kordex.parser.StringParser

/**
 * Argument converter for [ForumTag] arguments.
 *
 * Accepts a callable [channelGetter] property which may be used to extract a forum channel from another argument.
 */
@Converter(
	"tag",
	types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
	imports = [
		"dev.kordex.core.utils.suggestStringCollection",
		"dev.kord.core.entity.channel.ForumChannel",
	],
	builderFields = [
		"public var channelGetter: (suspend () -> ForumChannel?)? = null"
	],
	builderInitStatements = [
		"" +
			"        autoComplete { event ->\n" +
			"            try {\n" +
			"                val tags = TagConverter.getTags(event, channelGetter)\n" +
			"                \n" +
			"                suggestStringCollection(tags.map { it.name })\n" +
			"            } catch (e: Exception) {\n" +
			"               // kordLogger.warn{ \"Failed to process autocomplete event for tag converter: " +
			"\${e.reason}\" }\n" +
			"            }\n" +
			"        }"
	]
)
@Suppress("UnusedPrivateMember")
public class TagConverter(
	private val channelGetter: (suspend () -> ForumChannel?)? = null,

	override var validator: Validator<ForumTag> = null,
) : SingleConverter<ForumTag>() {
	public override val signatureType: Key = CoreTranslations.Converters.Tag.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val input: String = named ?: parser?.parseNext()?.data ?: return false

		this.parsed = getTag(input, context)

		return true
	}

	private suspend fun getTag(input: String, context: CommandContext): ForumTag {
		val tags: List<ForumTag> = getTags(context, channelGetter)
		val locale: ULocale = ULocale(context.getLocale().toString())

		val tag: ForumTag = tags.firstOrNull {
			it.name.equals(input, true)
		} ?: tags.firstOrNull {
			if (locale.isRightToLeft) {
				it.name.endsWith(input, true)
			} else {
				it.name.startsWith(input, true)
			}
		} ?: tags.firstOrNull {
			it.name.contains(input, true)
		} ?: throw DiscordRelayedException(
			CoreTranslations.Converters.Tag.Error.unknownTag
				.withContext(context)
				.withOrdinalPlaceholders(input)
		)

		return tag
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<StringChoiceBuilder> =
		wrapOption(arg.displayName, arg.description) {
			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue: String = (option as? StringOptionValue)?.value ?: return false

		this.parsed = getTag(optionValue, context)

		return true
	}

	public companion object : KordExKoinComponent {
		public suspend fun getTags(
			context: CommandContext,
			getter: (suspend () -> ForumChannel?)? = null,
		): List<ForumTag> {
			val channel: ForumChannel? = if (getter != null) {
				getter()
			} else {
				val thread = context.getChannel().asChannelOfOrNull<ThreadChannel>()

				thread?.parent?.asChannelOfOrNull<ForumChannel>()
			}

			if (channel == null) {
				val key = if (getter == null) {
					CoreTranslations.Converters.Tag.Error.wrongChannelType
				} else {
					CoreTranslations.Converters.Tag.Error.wrongChannelTypeWithGetter
				}

				throw DiscordRelayedException(
					key.withContext(context)
				)
			}

			return channel.availableTags
		}

		public suspend fun getTags(
			event: AutoCompleteInteractionCreateEvent,
			getter: (suspend () -> ForumChannel?)? = null,
		): List<ForumTag> {
			val channel: ForumChannel? = if (getter != null) {
				getter()
			} else {
				val thread = event.interaction.getChannel().asChannelOfOrNull<ThreadChannel>()

				thread?.parent?.asChannelOfOrNull<ForumChannel>()
			}

			if (channel == null) {
				throw DiscordRelayedException(
					if (getter == null) {
						CoreTranslations.Converters.Tag.Error.wrongChannelType
					} else {
						CoreTranslations.Converters.Tag.Error.wrongChannelTypeWithGetter
					}
						.withLocale(event.getLocale())
				)
			}

			return channel.availableTags
		}
	}
}
