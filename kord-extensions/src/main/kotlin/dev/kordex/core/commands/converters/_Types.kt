/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
