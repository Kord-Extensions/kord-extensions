/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.converters.builders.OptionalConverterBuilder
import dev.kord.common.annotation.KordPreview

/**
 * Abstract base class for an optional single converter.
 *
 * This works just like [SingleConverter], but the value can be nullable and it can never be required.
 *
 * @property outputError Whether the argument parser should output parsing errors on invalid arguments.
 * @property validator Validation lambda, which may throw a DiscordRelayedException if required.
 */
public abstract class OptionalConverter<T : Any>(
    public val outputError: Boolean = false,
    override var validator: Validator<T?> = null
) : Converter<T, T?, String, Boolean>(false), SlashCommandConverter {
    /**
     * The parsed value.
     *
     * This should be set by the converter during the course of the [parse] function.
     */
    public override var parsed: T? = null

    /** Access to the converter builder, perhaps a bit more hacky than it should be but whatever. **/
    public open lateinit var builder: OptionalConverterBuilder<T>

    /** @suppress Internal function used by converter builders. **/
    public open fun withBuilder(
        builder: OptionalConverterBuilder<T>
    ): OptionalConverter<T> {
        this.builder = builder
        this.genericBuilder = builder

        return this
    }
}
