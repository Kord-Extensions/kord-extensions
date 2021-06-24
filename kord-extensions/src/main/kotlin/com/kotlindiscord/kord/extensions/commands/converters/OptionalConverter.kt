@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.commands.converters

import dev.kord.common.annotation.KordPreview

/**
 * Abstract base class for an optional single converter.
 *
 * This works just like [SingleConverter], but the value can be nullable and it can never be required.
 *
 * @property outputError Whether the argument parser should output parsing errors on invalid arguments.
 * @property validator Validation lambda, which may throw a CommandException if required.
 */
public abstract class OptionalConverter<T : Any?>(
    public val outputError: Boolean = false,
    override var validator: Validator<T?> = null
) : Converter<T, T?, String, Boolean>(false), SlashCommandConverter {
    /**
     * The parsed value.
     *
     * This should be set by the converter during the course of the [parse] function.
     */
    public override var parsed: T? = null
}
