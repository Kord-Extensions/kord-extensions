/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands

import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.i18n.types.Key
import kotlin.reflect.KClass

public class OptionWrapper<T : OptionsBuilder> @InternalAPI constructor(
	public var displayName: Key,
	public var description: Key,

	public val body: suspend T.() -> Unit,
	public val type: KClass<T>,
) {
	public val modifiers: MutableList<suspend T.() -> Unit> = mutableListOf()

	public fun modify(body: suspend T.() -> Unit) {
		modifiers.add(body)
	}

	public suspend fun apply(builder: T): T {
		body(builder)
		modifiers.forEach { it(builder) }

		return builder
	}
}

@OptIn(InternalAPI::class)
public inline fun <reified T : OptionsBuilder> wrapOption(
	displayName: Key,
	description: Key,

	noinline body: suspend T.() -> Unit,
): OptionWrapper<T> = OptionWrapper<T>(displayName, description, body, T::class)
