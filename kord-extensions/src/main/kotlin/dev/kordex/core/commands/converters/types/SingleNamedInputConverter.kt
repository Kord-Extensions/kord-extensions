/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.types

import dev.kordex.core.commands.converters.Converter

public abstract class SingleNamedInputConverter<InputType : Any?, OutputType : Any?, ResultType : Any>(
	public override val required: Boolean = true,
) : Converter<InputType, OutputType, String, ResultType>()
