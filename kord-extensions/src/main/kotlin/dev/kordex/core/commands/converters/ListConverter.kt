/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters

import dev.kordex.core.commands.converters.builders.ListConverterBuilder
import dev.kordex.core.commands.converters.types.MultiNamedInputConverter

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
	override var validator: Validator<List<T>> = null,
) : MultiNamedInputConverter<List<T>, List<T>, Int>(required), SlashCommandConverter {
	/**
	 * The parsed value.
	 *
	 * This should be set by the converter during the course of the [parse] function.
	 */
	public override var parsed: List<T> = listOf()

	/** Access to the converter builder, perhaps a bit more hacky than it should be but whatever. **/
	public open lateinit var builder: ListConverterBuilder<T>

	/** @suppress Internal function used by converter builders. **/
	public open fun withBuilder(
		builder: ListConverterBuilder<T>,
	): ListConverter<T> {
		this.builder = builder
		this.genericBuilder = builder

		return this
	}
}
