@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.commands.converters

import dev.kord.common.annotation.KordPreview

/**
 * Abstract base class for a defaulting coalescing converter.
 *
 * Coalescing converters take a list of multiple arguments, and consumes as many arguments as it can, combining
 * those arguments into a single value. Upon reaching an argument that can't be consumed, the converter stores
 * its final result and tells the parser how many arguments it managed to consume. The parser will continue
 * processing the unused arguments, passing them to the remaining converters.
 *
 * A defaulting coalescing converter has a default value that will be provided if nothing could be parsed.
 *
 * You can create an optional coalescing converter of your own by extending this class.
 *
 * @property validator Validation lambda, which may throw a CommandException if required.
 */
public abstract class DefaultingCoalescingConverter<T : Any>(
    defaultValue: T,
    override var validator: Validator<T> = null
) : Converter<List<T>, T, List<String>, Int>(
    false
) {
    /**
     * The parsed value.
     *
     * This should be set by the converter during the course of the [parse] function.
     */
    public override var parsed: T = defaultValue
}
