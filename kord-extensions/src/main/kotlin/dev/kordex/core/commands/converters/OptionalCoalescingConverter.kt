/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters

import dev.kordex.core.commands.converters.builders.OptionalCoalescingConverterBuilder
import dev.kordex.core.commands.converters.types.MultiNamedInputConverter

/**
 * Abstract base class for an optional coalescing converter.
 *
 * Coalescing converters take a list of multiple arguments, and consumes as many arguments as it can, combining
 * those arguments into a single value. Upon reaching an argument that can't be consumed, the converter stores
 * its final result and tells the parser how many arguments it managed to consume. The parser will continue
 * processing the unused arguments, passing them to the remaining converters.
 *
 * An optional coalescing converter has a nullable type - you'll get `null` from it if nothing could be parsed.
 *
 * You can create an optional coalescing converter of your own by extending this class.
 *
 * @property outputError Whether the argument parser should output parsing errors on invalid arguments.
 * @property validator Validation lambda, which may throw a DiscordRelayedException if required.
 */
public abstract class OptionalCoalescingConverter<T : Any>(
	public val outputError: Boolean = false,
	override var validator: Validator<T?> = null,
) : MultiNamedInputConverter<List<T>, T?, Int>(false), SlashCommandConverter {
	/**
	 * The parsed value.
	 *
	 * This should be set by the converter during the course of the [parse] function.
	 */
	public override var parsed: T? = null

	/** Access to the converter builder, perhaps a bit more hacky than it should be but whatever. **/
	public open lateinit var builder: OptionalCoalescingConverterBuilder<T>

	/** @suppress Internal function used by converter builders. **/
	public open fun withBuilder(
		builder: OptionalCoalescingConverterBuilder<T>,
	): OptionalCoalescingConverter<T> {
		this.builder = builder
		this.genericBuilder = builder

		return this
	}
}
