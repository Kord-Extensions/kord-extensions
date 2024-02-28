/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
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
 * Coalescing argument converter that simply joins all arguments with spaces to produce a single string.
 *
 * This converter will consume all remaining arguments.
 *
 * @see coalescedString
 */
@Converter(
	"string",

	types = [ConverterType.COALESCING, ConverterType.DEFAULTING, ConverterType.OPTIONAL]
)
public class StringCoalescingConverter(
	shouldThrow: Boolean = false,
	override var validator: Validator<String> = null,
) : CoalescingConverter<String>(shouldThrow) {
	override val signatureTypeString: String = "converters.string.signatureType"
	override val showTypeInSignature: Boolean = false
	override val bundle: String = DEFAULT_KORDEX_BUNDLE

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: List<String>?): Int {
		this.parsed = named?.joinToString(" ") ?: parser?.consumeRemaining() ?: return 0

		return parsed.length
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false
		this.parsed = optionValue

		return true
	}
}
