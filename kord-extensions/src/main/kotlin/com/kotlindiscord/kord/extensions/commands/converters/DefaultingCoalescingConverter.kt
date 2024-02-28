/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.converters.builders.DefaultingCoalescingConverterBuilder

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
 * @property outputError Whether the argument parser should output parsing errors on invalid arguments.
 * @property validator Validation lambda, which may throw a DiscordRelayedException if required.
 */
public abstract class DefaultingCoalescingConverter<T : Any>(
	defaultValue: T,
	public val outputError: Boolean = false,
	override var validator: Validator<T> = null,
) : Converter<List<T>, T, List<String>, Int>(false), SlashCommandConverter {
	/**
	 * The parsed value.
	 *
	 * This should be set by the converter during the course of the [parse] function.
	 */
	public override var parsed: T = defaultValue

	/** Access to the converter builder, perhaps a bit more hacky than it should be but whatever. **/
	public open lateinit var builder: DefaultingCoalescingConverterBuilder<T>

	/** @suppress Internal function used by converter builders. **/
	public open fun withBuilder(
		builder: DefaultingCoalescingConverterBuilder<T>,
	): DefaultingCoalescingConverter<T> {
		this.builder = builder
		this.genericBuilder = builder

		return this
	}
}
