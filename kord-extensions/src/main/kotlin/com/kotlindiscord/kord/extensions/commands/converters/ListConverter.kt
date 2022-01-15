/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.commands.converters

import dev.kord.common.annotation.KordPreview

/**
 * Abstract base class for a multi converter.
 *
 * List converters take a list of multiple arguments, consuming as many arguments as it can to produce a list
 * of resulting values. Upon reaching an argument that can't be consumed, the converter stores everything it could
 * convert and tells the parser how many arguments it managed to consume. The parser will continue processing the
 * unused arguments, passing them to the remaining converters.
 *
 * You can create a list converter of your own by extending this class.
 *
 * @property validator Validation lambda, which may throw a DiscordRelayedException if required.
 */
public abstract class ListConverter<T : Any>(
    required: Boolean = true,
    override var validator: Validator<List<T>> = null
) : Converter<List<T>, List<T>, List<String>, Int>(required) {
    /**
     * The parsed value.
     *
     * This should be set by the converter during the course of the [parse] function.
     */
    public override var parsed: List<T> = listOf()
}
