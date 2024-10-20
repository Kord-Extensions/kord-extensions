/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters

import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.commands.converters.builders.ConverterBuilder
import dev.kordex.core.commands.converters.types.SingleNamedInputConverter
import dev.kordex.core.i18n.types.Key

/**
 * Abstract base class for a single converter.
 *
 * Single converters take a single string argument, transforming it into a single resulting value. Single converters
 * are always required.
 *
 * You can convert a [SingleConverter] instance to a defaulting, optional or multi converter
 * using [toDefaulting]. [toList] or [toOptional] respectively.
 *
 * You can create a single converter of your own by extending this class.
 *
 * @property validator Validation lambda, which may throw a DiscordRelayedException if required.
 */
public abstract class SingleConverter<T : Any>(
	override var validator: Validator<T> = null,
) : SingleNamedInputConverter<T, T, Boolean>(true), SlashCommandConverter {
	/**
	 * The parsed value.
	 *
	 * This should be set by the converter during the course of the [parse] function.
	 */
	public override lateinit var parsed: T

	/** Access to the converter builder, perhaps a bit more hacky than it should be but whatever. **/
	public open lateinit var builder: ConverterBuilder<T>

	/** @suppress Internal function used by converter builders. **/
	public open fun withBuilder(
		builder: ConverterBuilder<T>,
	): SingleConverter<T> {
		this.builder = builder
		this.genericBuilder = builder

		return this
	}

	/**
	 * Wrap this single converter with a [SingleToListConverter], which is a special converter that will act like a
	 * [ListConverter] using the same logic of this converter.
	 *
	 * Your converter should be designed with this pattern in mind. If that's not possible, please override this
	 * function and throw an exception in the body.
	 *
	 * For more information on the parameters, see [Converter].
	 *
	 * @param required Whether command parsing should fail if no arguments could be converted.
	 *
	 * @param signatureType Optionally, a signature type string to use instead of the one this converter
	 * provides.
	 *
	 * @param showTypeInSignature Optionally, override this converter's setting for showing the type string in a
	 * generated command signature.
	 *
	 * @param errorType Optionally, a longer type string to be shown in errors instead of the one this converter
	 * provides.
	 */
	@ConverterToMulti
	public open fun toList(
		required: Boolean = true,
		signatureType: Key? = null,
		showTypeInSignature: Boolean? = null,
		errorType: Key? = null,
		nestedValidator: Validator<List<T>> = null,
	): ListConverter<T> = SingleToListConverter(
		required,
		this,
		signatureType,
		showTypeInSignature,
		errorType,
		nestedValidator
	)

	/**
	 * Wrap this single converter with a [SingleToOptionalConverter], which is a special converter that will act like
	 * an [OptionalConverter] using the same logic of this converter.
	 *
	 * Your converter should be designed with this pattern in mind. If that's not possible, please override this
	 * function and throw an exception in the body.
	 *
	 * For more information on the parameters, see [Converter].
	 *
	 * @param signatureType Optionally, a signature type string to use instead of the one this converter
	 * provides.
	 *
	 * @param showTypeInSignature Optionally, override this converter's setting for showing the type string in a
	 * generated command signature.
	 *
	 * @param errorType Optionally, a longer type string to be shown in errors instead of the one this converter
	 * provides.
	 *
	 * @param outputError Optionally, provide `true` to fail parsing and return errors if the converter throws a
	 * [DiscordRelayedException] (instead of continuing).
	 */
	@ConverterToOptional
	public open fun toOptional(
		signatureType: Key? = null,
		showTypeInSignature: Boolean? = null,
		errorType: Key? = null,
		outputError: Boolean = false,
		nestedValidator: Validator<T?> = null,
	): OptionalConverter<T> = SingleToOptionalConverter(
		this,
		signatureType,
		showTypeInSignature,
		errorType,
		outputError,
		nestedValidator
	)

	/**
	 * Wrap this single converter with a [SingleToDefaultingConverter], which is a special converter that will act like
	 * a [DefaultingConverter] using the same logic of this converter.
	 *
	 * Your converter should be designed with this pattern in mind. If that's not possible, please override this
	 * function and throw an exception in the body.
	 *
	 * For more information on the parameters, see [Converter].
	 *
	 * @param defaultValue The default value to use when an argument can't be converted.
	 * @param outputError Whether the argument parser should output parsing errors on invalid arguments.
	 * @param signatureType Optionally, a signature type string to use instead of the one this converter
	 * provides.
	 *
	 * @param showTypeInSignature Optionally, override this converter's setting for showing the type string in a
	 * generated command signature.
	 *
	 * @param errorType Optionally, a longer type string to be shown in errors instead of the one this converter
	 * provides.
	 */
	@ConverterToDefaulting
	public open fun toDefaulting(
		defaultValue: T,
		outputError: Boolean = false,
		signatureType: Key? = null,
		showTypeInSignature: Boolean? = null,
		errorType: Key? = null,
		nestedValidator: Validator<T> = null,
	): DefaultingConverter<T> = SingleToDefaultingConverter(
		this,
		defaultValue = defaultValue,
		outputError = outputError,
		newSignatureType = signatureType,
		newShowTypeInSignature = showTypeInSignature,
		newErrorType = errorType,
		validator = nestedValidator
	)
}
