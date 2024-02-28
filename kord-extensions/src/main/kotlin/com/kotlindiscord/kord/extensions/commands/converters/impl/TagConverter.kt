/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.ibm.icu.util.ULocale
import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.i18n.DEFAULT_KORDEX_BUNDLE
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.utils.getLocale
import dev.kord.common.entity.ForumTag
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.ForumChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import org.koin.core.component.inject

/**
 * Argument converter for [ForumTag] arguments.
 *
 * Accepts a callable [channelGetter] property which may be used to extract a forum channel from another argument.
 */
@Converter(
	"tag",
	types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
	imports = [
		"com.kotlindiscord.kord.extensions.utils.suggestStringCollection",
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
	public override val signatureTypeString: String = "converters.tag.signatureType"
	override val bundle: String = DEFAULT_KORDEX_BUNDLE

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
			context.translate(
				"converters.tag.error.unknownTag",
				replacements = arrayOf(input)
			)
		)

		return tag
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue: String = (option as? StringOptionValue)?.value ?: return false

		this.parsed = getTag(optionValue, context)

		return true
	}

	public companion object : KordExKoinComponent {
		/** Translations provider, for retrieving translations. **/
		private val translationsProvider: TranslationsProvider by inject()

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
				throw DiscordRelayedException(
					context.translate(
						if (getter == null) {
							"converters.tag.error.wrongChannelType"
						} else {
							"converters.tag.error.wrongChannelTypeWithGetter"
						}
					)
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
					translationsProvider.translate(
						if (getter == null) {
							"converters.tag.error.wrongChannelType"
						} else {
							"converters.tag.error.wrongChannelTypeWithGetter"
						},

						event.getLocale(),
						DEFAULT_KORDEX_BUNDLE
					)
				)
			}

			return channel.availableTags
		}
	}
}
