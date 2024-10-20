/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.core.commands

import dev.kord.rest.builder.interaction.BaseChoiceBuilder
import dev.kord.rest.builder.interaction.IntegerOptionBuilder
import dev.kord.rest.builder.interaction.NumberOptionBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.i18n.types.Key
import kotlin.reflect.KClass

public sealed class ChoiceOptionWrapper<B : BaseChoiceBuilder<*, *>, T> (
	displayName: Key,
	description: Key,

	body: suspend B.() -> Unit,
	type: KClass<B>,
) : OptionWrapper<B>(displayName, description, body, type) {
	public val choices: MutableList<ChoiceWrapper> = mutableListOf()

	public fun choice(name: Key, value: T) {
		choices.add(ChoiceWrapper(name, value))
	}

	public inner class ChoiceWrapper(public val name: Key, public val value: T)

	public class Integer @InternalAPI constructor(
		displayName: Key,
		description: Key,

		body: suspend IntegerOptionBuilder.() -> Unit,
	) : ChoiceOptionWrapper<IntegerOptionBuilder, Long>(displayName, description, body, IntegerOptionBuilder::class)

	public class Number @InternalAPI constructor(
		displayName: Key,
		description: Key,

		body: suspend NumberOptionBuilder.() -> Unit,
	) : ChoiceOptionWrapper<NumberOptionBuilder, Double>(displayName, description, body, NumberOptionBuilder::class)

	public class String @InternalAPI constructor(
		displayName: Key,
		description: Key,

		body: suspend StringChoiceBuilder.() -> Unit,
	) : ChoiceOptionWrapper<StringChoiceBuilder, kotlin.String>(displayName, description, body, StringChoiceBuilder::class)
}

@OptIn(InternalAPI::class)
public suspend fun wrapIntegerOption(
	displayName: Key,
	description: Key,

	body: suspend IntegerOptionBuilder.() -> Unit,
): ChoiceOptionWrapper.Integer {
	val wrapper = ChoiceOptionWrapper.Integer(displayName, description, body)
	val kord = wrapper.toKord() as BaseChoiceBuilder<*, *>

	if (kord.choices?.isNotEmpty() == true) {
		error("Add choices to the wrapper, not directly to the builder.")
	}

	return wrapper
}

@OptIn(InternalAPI::class)
public suspend fun wrapNumberOption(
	displayName: Key,
	description: Key,

	body: suspend NumberOptionBuilder.() -> Unit,
): ChoiceOptionWrapper.Number {
	val wrapper = ChoiceOptionWrapper.Number(displayName, description, body)
	val kord = wrapper.toKord() as BaseChoiceBuilder<*, *>

	if (kord.choices?.isNotEmpty() == true) {
		error("Add choices to the wrapper, not directly to the builder.")
	}

	return wrapper
}

@OptIn(InternalAPI::class)
public suspend fun wrapStringOption(
	displayName: Key,
	description: Key,

	body: suspend StringChoiceBuilder.() -> Unit,
): ChoiceOptionWrapper.String {
	val wrapper = ChoiceOptionWrapper.String(displayName, description, body)
	val kord = wrapper.toKord() as BaseChoiceBuilder<*, *>

	if (kord.choices?.isNotEmpty() == true) {
		error("Add choices to the wrapper, not directly to the builder.")
	}

	return wrapper
}
