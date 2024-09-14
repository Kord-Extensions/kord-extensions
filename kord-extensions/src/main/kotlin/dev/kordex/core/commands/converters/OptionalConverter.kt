/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters

import dev.kordex.core.commands.converters.builders.OptionalConverterBuilder
import dev.kordex.core.commands.converters.types.SingleNamedInputConverter

/**
 * Abstract base class for an optional single converter.
 *
 * This works just like [SingleConverter], but the value can be nullable, and it can never be required.
 *
 * @property outputError Whether the argument parser should output parsing errors on invalid arguments.
 * @property validator Validation lambda, which may throw a DiscordRelayedException if required.
 */
public abstract class OptionalConverter<T : Any>(
	public val outputError: Boolean = false,
	override var validator: Validator<T?> = null,
) : SingleNamedInputConverter<T, T?, Boolean>(false), SlashCommandConverter {
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
		builder: OptionalConverterBuilder<T>,
	): OptionalConverter<T> {
		this.builder = builder
		this.genericBuilder = builder

		return this
	}
}
