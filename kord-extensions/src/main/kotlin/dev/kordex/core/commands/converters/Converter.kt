/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters

import dev.kord.core.Kord
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.converters.builders.ConverterBuilder
import dev.kordex.core.commands.converters.builders.ValidationContext
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.parser.StringParser
import org.koin.core.component.inject
import kotlin.reflect.KProperty

/**
 * Base class for an argument converter.
 *
 * Argument converters are in charge of taking either one or several string arguments, and converting them into some
 * other type. They're a convenience for people working with the commands framework.
 *
 * You probably don't want to subclass this directly. There are three direct subclasses you can look at if you'd like
 * to implement your own converters.
 *
 * @param required Whether this converter must succeed for a command invocation to be valid.
 *
 * @param InputType TypeVar representing the specific result type this converter represents
 * @param OutputType TypeVar representing the final type of the parsed argument which is given to the bot developer
 * @param NamedInputType TypeVar representing how this converter receives named arguments - either `String` or
 * `List<String>`
 * @param ResultType TypeVar representing how this converter signals whether it succeeded - either `Boolean` or `Int`
 */
public abstract class Converter<InputType : Any?, OutputType : Any?, NamedInputType : Any, ResultType : Any>(
	public open val required: Boolean = true,
) : KordExKoinComponent {
	/** This is pretty hacky, but there aren't many better options. **/
	internal lateinit var genericBuilder: ConverterBuilder<OutputType>

	/** Current instance of the bot. **/
	public open val bot: ExtensibleBot by inject()

	/** Kord instance, backing the ExtensibleBot. **/
	public val kord: Kord by inject()

	/**
	 * The parsed value.
	 *
	 * This should be set by the converter during the course of the [parse] function.
	 */
	public abstract var parsed: OutputType

	/** Validation lambda, which may throw a [DiscordRelayedException] if required. **/
	public open var validator: Validator<OutputType> = null

	/** This will be set to true by the argument parser if the conversion succeeded. **/
	public var parseSuccess: Boolean = false

	/** For commands with generated signatures, set whether the type string should be shown in the signature. **/
	public open val showTypeInSignature: Boolean = true

	/**
	 * Translation key pointing to a short string describing the type of data this converter handles. Should be very
	 * short.
	 */
	public abstract val signatureType: Key

	/**
	 * If the [signatureType] isn't sufficient, you can optionally provide a translation key pointing to a
	 * longer type string to use for error messages.
	 */
	public open val errorType: Key? = null

	/** Argument object containing this converter and its metadata. **/
	public open lateinit var argumentObj: Argument<*>

	/** For delegation, retrieve the parsed value if it's been set, or null if it hasn't. **/
	public operator fun getValue(thisRef: Arguments, property: KProperty<*>): OutputType {
		try {
			return if (::genericBuilder.isInitialized && genericBuilder.mutator != null) {
				genericBuilder.mutator!!(parsed)
			} else {
				parsed
			}
		} catch (e: UninitializedPropertyAccessException) {
			throw UninitializedPropertyAccessException(
				"Parsed value accessed before it has been filled." +
					"\n\n" +
					"This may happen when arguments are accessed from other argument builders out of order, users " +
					"provide arguments in the wrong order, or previous arguments are accessed in autoComplete " +
					"builders and `parseForAutocomplete` is not overridden to be `true` in your `Arguments` subclass." +
					"\n\n" +
					"If you believe this exception was thrown due to a bug, please raise an issue on the Kord " +
					"Extensions GitHub, or ask about it on the Kord Extensions Discord server.",

				e
			)
		}
	}

	/**
	 * Given a Throwable encountered during the [parse] function, return a human-readable string to display on Discord.
	 *
	 * For multi converters, this is only called when the converter is required. The default behaviour simply
	 * re-throws the Throwable (or returns the reason if it's a DiscordRelayedException), so you only need to override
	 * this if you want to do something else.
	 */
	public open suspend fun handleError(
		t: Throwable,
		context: CommandContext,
	): Key = if (t is DiscordRelayedException) t.reason.withContext(context) else throw t

	/** Call the validator lambda, if one was provided. **/
	public open suspend fun validate(context: CommandContext) {
		validator?.let { actualValidator ->
			val validationContext = ValidationContext(parsed, context)

			actualValidator.invoke(validationContext)

			validationContext.throwIfFailed()
		}
	}

	/**
	 * Process the string in the given [parser], converting it into a new value.
	 *
	 * The resulting value should be stored in [parsed] - this will not be done for you.
	 *
	 * If you'd like to return more detailed feedback to the user on invalid input, you can throw a
	 * [DiscordRelayedException] here.
	 *
	 * @param parser [StringParser] used to parse the command, if any
	 * @param context MessageCommand context object, containing the event, message, and other command-related things
	 *
	 * @return Whether you managed to convert the argument. If you don't want to provide extra context to the user,
	 * simply return `false` or `0` depending on your converter type - the command system will generate an error
	 * message for you.
	 *
	 * @see Converter
	 */
	public abstract suspend fun parse(
		parser: StringParser?,
		context: CommandContext,
		named: NamedInputType? = null,
	): ResultType

	/**
	 * Return a translated, formatted error string.
	 *
	 * This will attempt to use the [errorType], falling back to [signatureType].
	 */
	public open suspend fun getErrorString(context: CommandContext): String =
		getErrorKey().withContext(context).translate()

	/**
	 * Return a [Key] representing a pre-translated error string.
	 *
	 * This will attempt to use the [errorType], falling back to [signatureType].
	 */
	public open suspend fun getErrorKey(): Key =
		errorType ?: signatureType
}
