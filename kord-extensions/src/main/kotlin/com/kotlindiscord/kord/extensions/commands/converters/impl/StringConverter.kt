/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.i18n.DEFAULT_KORDEX_BUNDLE
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Coalescing argument that simply returns the argument as it was given.
 *
 * The multi version of this converter (via [toList]) will consume all remaining arguments.
 *
 * @property maxLength The maximum length allowed for this argument.
 * @property minLength The minimum length allowed for this argument.
 */
@Converter(
	"string",

	types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],

	builderFields = [
		"public var maxLength: Int? = null",
		"public var minLength: Int? = null",
	]
)
public class StringConverter(
	public val maxLength: Int? = null,
	public val minLength: Int? = null,
	override var validator: Validator<String> = null,
) : SingleConverter<String>() {
	override val signatureTypeString: String = "converters.string.signatureType"
	override val showTypeInSignature: Boolean = false
	override val bundle: String = DEFAULT_KORDEX_BUNDLE

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		this.parsed = arg

		if (minLength != null && this.parsed.length < minLength) {
			throw DiscordRelayedException(
				context.translate(
					"converters.string.error.invalid.tooShort",
					replacements = arrayOf(arg, minLength)
				)
			)
		}

		if (maxLength != null && this.parsed.length > maxLength) {
			throw DiscordRelayedException(
				context.translate(
					"converters.string.error.invalid.tooLong",
					replacements = arrayOf(arg, maxLength)
				)
			)
		}

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		StringChoiceBuilder(arg.displayName, arg.description).apply {
			this@apply.maxLength = this@StringConverter.maxLength
			this@apply.minLength = this@StringConverter.minLength

			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false
		this.parsed = optionValue

		return true
	}
}
