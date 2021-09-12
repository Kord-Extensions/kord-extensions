@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.commands.converters

import dev.kord.common.annotation.KordPreview

/**
 * Abstract base class for a defaulting converter.
 *
 * Single converters take a single string argument, transforming it into a single resulting value. A default value
 * will be provided in case parsing fails.
 *
 * You can create a defaulting converter of your own by extending this class.
 *
 * @property outputError Whether the argument parser should output parsing errors on invalid arguments.
 * @property validator Validation lambda, which may throw a DiscordRelayedException if required.
 */
public abstract class DefaultingConverter<T : Any>(
    defaultValue: T,
    public val outputError: Boolean = false,
    override var validator: Validator<T> = null
) : Converter<T, T, String, Boolean>(false), SlashCommandConverter {
    /**
     * The parsed value.
     *
     * This should be set by the converter during the course of the [parse] function.
     */
    public override var parsed: T = defaultValue
}
