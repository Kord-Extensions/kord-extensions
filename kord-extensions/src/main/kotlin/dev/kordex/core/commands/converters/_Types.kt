/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("Filename")

package dev.kordex.core.commands.converters

import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kordex.core.commands.converters.builders.ValidationContext

/** Type alias representing a validator callable. Keeps things relatively maintainable. **/
public typealias Validator<T> = (suspend ValidationContext<T>.() -> Unit)?

/** Type alias representing a mutator callable. Keeps things relatively maintainable. **/
public typealias Mutator<T> = ((value: T) -> T)?

/** Type alias representing an autocomplete callable. **/
public typealias AutoCompleteCallback =
	(suspend AutoCompleteInteraction.(event: AutoCompleteInteractionCreateEvent) -> Unit)?
