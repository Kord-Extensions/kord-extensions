/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.builders

import com.kotlindiscord.kord.extensions.InvalidArgumentException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.AutoCompleteCallback
import com.kotlindiscord.kord.extensions.commands.converters.Converter
import com.kotlindiscord.kord.extensions.commands.converters.Mutator
import com.kotlindiscord.kord.extensions.commands.converters.Validator

/** Base abstract class for all converter builders. **/
public abstract class ConverterBuilder<T> {
    /** Converter display name. Required. **/
    public open lateinit var name: String

    /** Converter description. Required. **/
    public open lateinit var description: String

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
