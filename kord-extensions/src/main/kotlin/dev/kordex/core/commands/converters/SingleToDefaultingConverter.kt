/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters

import dev.kord.core.entity.interaction.OptionValue
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.OptionWrapper
import dev.kordex.core.i18n.types.Key
import dev.kordex.parser.StringParser

/**
 * A special [SingleConverter] that wraps another [SingleConverter], effectively turning it into an optional
 * converter with the same logic, but with a default value if parsing fails.
 *
 * @param singleConverter The [SingleConverter] to wrap.
 *
 * @param newSignatureType An optional signature type string to override the one set in [singleConverter].
 * @param newShowTypeInSignature An optional boolean to override the [showTypeInSignature] setting set in
 * [singleConverter].
 * @param newErrorTypeString An optional error type string to override the one set in [singleConverter].
 */
public class SingleToDefaultingConverter<T : Any>(
	public val singleConverter: SingleConverter<T>,
	defaultValue: T,
	outputError: Boolean = false,

	newSignatureType: Key? = null,
	newShowTypeInSignature: Boolean? = null,
	newErrorType: Key? = null,

	override var validator: Validator<T> = null,
) : DefaultingConverter<T>(defaultValue, outputError = outputError) {
	override val signatureType: Key = newSignatureType ?: singleConverter.signatureType
	override val showTypeInSignature: Boolean = newShowTypeInSignature ?: singleConverter.showTypeInSignature
	override val errorType: Key? = newErrorType ?: singleConverter.errorType

	private val dummyArgs = Arguments()

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val token = parser?.peekNext()
		val result = singleConverter.parse(parser, context, named ?: token?.data)

		if (result) {
			this.parsed = singleConverter.getValue(dummyArgs, singleConverter::parsed)

			if (named == null) {
				parser?.parseNext()  // Move the cursor ahead
			}

			return true
		}

		return false
	}

	override suspend fun handleError(
		t: Throwable,
		context: CommandContext,
	): Key = singleConverter.handleError(t, context)

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<*> {
		val option = singleConverter.toSlashOption(arg)

		option.modify {
			required = false
		}

		return option
	}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val result = singleConverter.parseOption(context, option)

		if (result) {
			this.parsed = singleConverter.getValue(dummyArgs, singleConverter::parsed)
		}

		return result
	}
}
