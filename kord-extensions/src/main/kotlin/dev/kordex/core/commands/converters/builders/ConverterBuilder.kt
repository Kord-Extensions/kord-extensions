/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.builders

import dev.kordex.core.InvalidArgumentException
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.converters.AutoCompleteCallback
import dev.kordex.core.commands.converters.Converter
import dev.kordex.core.commands.converters.Mutator
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.i18n.types.Key

/** Base abstract class for all converter builders. **/
public abstract class ConverterBuilder<T> {
	/** Converter display name. Required. **/
	public open lateinit var name: Key

	/** Converter description. Required. **/
	public open lateinit var description: Key

	/** Mutator, used to mutate the parsed value before it's presented. **/
	public open var mutator: Mutator<T> = null

	/** Validator, used for argument validation. **/
	protected open var validator: Validator<T> = null

	/** Auto-complete callback. **/
	public open var autoCompleteCallback: AutoCompleteCallback = null

	/** Register the autocomplete callback for this converter. **/
	public open fun autoComplete(body: AutoCompleteCallback) {
		autoCompleteCallback = body
	}

	/** Register the mutator for this converter, allowing you to modify the final value. **/
	public open fun mutate(body: Mutator<T>) {
		mutator = body
	}

	/** Register the validator for this converter, allowing you to validate the final value. **/
	public open fun validate(body: Validator<T>) {
		validator = body
	}

	/** Using the data in this builder, create a converter and apply it to an [Arguments] object. **/
	public abstract fun build(arguments: Arguments): Converter<*, *, *, *>

	/** Validate that this builder is set up properly. **/
	public open fun validateArgument() {
		if (!this::name.isInitialized) {
			throw InvalidArgumentException(this, "Required field not provided: name")
		}

		if (!this::description.isInitialized) {
			throw InvalidArgumentException(this, "Required field not provided: description")
		}

		if (this is ChoiceConverterBuilder<*> && this.choices.isNotEmpty() && this.autoCompleteCallback != null) {
			throw InvalidArgumentException(
				this,
				"One of either a map of choices or an autocomplete callback may be provided, but both are present"
			)
		}
	}

	/** Validate that this builder's value is allowable. **/
	public open suspend fun validateValue(commandContext: CommandContext, value: T) {
		if (validator != null) {
			val context = ValidationContext(value, commandContext)

			validator?.invoke(context)
			context.throwIfFailed()
		}
	}
}
