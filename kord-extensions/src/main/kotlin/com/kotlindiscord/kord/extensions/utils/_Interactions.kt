/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import dev.kord.core.behavior.interaction.suggestInt
import dev.kord.core.behavior.interaction.suggestNumber
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent

/** The max number of suggestions allowed. **/
public const val MAX_SUGGESTIONS: Int = 25

/**
 * Sealed interface representing matching strategies for autocomplete.
 *
 * @property test Lambda that should return `true` for acceptable values.
 */
public open class FilterStrategy(public val test: (provided: String, candidate: String) -> Boolean) {
    /** Filter options based on whether they contain the provided value. **/
    public object Contains : FilterStrategy({ provided, candidate ->
        candidate.contains(provided, true)
    })

    /** Filter options based on whether they start with the provided value. **/
    public object Prefix : FilterStrategy({ provided, candidate ->
        candidate.startsWith(provided, true)
    })

    /** Filter options based on whether they end with the provided value. **/
    public object Suffix : FilterStrategy({ provided, candidate ->
        candidate.endsWith(provided, true)
    })
}

/** Retrieve the option that's currently focused in the client. **/
public val AutoCompleteInteractionCreateEvent.focusedOption: OptionValue<*>
    get() = this.interaction.command.options.values.first { it.focused }

/** Retrieve the option that's currently focused in the client. **/
public val AutoCompleteInteraction.focusedOption: OptionValue<*>
    get() = this.command.options.values.first { it.focused }

/**
 * An [InteractionCommand] that contains the values the user filled so far.
 *
 * This might not contain all [options][InteractionCommand.options] and
 * [resolvedObjects][InteractionCommand.resolvedObjects], they will be available in a [ChatInputCommandInteraction].
 */
public val AutoCompleteInteraction.command: InteractionCommand get() = InteractionCommand(data.data, kord)

/** Use a map to populate an autocomplete interaction, filtering by comparing the input with the start of the keys. **/
public suspend inline fun AutoCompleteInteraction.suggestStringMap(
    map: Map<String, String>,
    strategy: FilterStrategy = FilterStrategy.Prefix
) {
    val option = focusedOption.value as? String
    var options = map

    if (option != null) {
        options = options.filterKeys { strategy.test(option, it) }
    }

    if (options.size > MAX_SUGGESTIONS) {
        options = options.entries.sortedBy { it.key }.take(MAX_SUGGESTIONS).associate { it.toPair() }
    }

    suggestString {
        options.forEach(::choice)
    }
}

/** Use a map to populate an autocomplete interaction, filtering by comparing the input with the start of the keys. **/
public suspend inline fun AutoCompleteInteraction.suggestIntMap(
    map: Map<String, Int>,
    strategy: FilterStrategy = FilterStrategy.Prefix
) {
    suggestLongMap(map.mapValues { it.value.toLong() }, strategy)
}

/** Use a map to populate an autocomplete interaction, filtering by comparing the input with the start of the keys. **/
public suspend inline fun AutoCompleteInteraction.suggestLongMap(
    map: Map<String, Long>,
    strategy: FilterStrategy = FilterStrategy.Prefix
) {
    val option = focusedOption.value as? String
    var options = map

    if (option != null) {
        options = options.filterKeys { strategy.test(option, it) }
    }

    if (options.size > MAX_SUGGESTIONS) {
        options = options.entries.sortedBy { it.key }.take(MAX_SUGGESTIONS).associate { it.toPair() }
    }

    suggestInt {
        options.forEach(::choice)
    }
}

/** Use a map to populate an autocomplete interaction, filtering by comparing the input with the start of the keys. **/
public suspend inline fun AutoCompleteInteraction.suggestDoubleMap(
    map: Map<String, Double>,
    strategy: FilterStrategy = FilterStrategy.Prefix
) {
    suggestNumberMap(map, strategy)
}

/** Use a map to populate an autocomplete interaction, filtering by comparing the input with the start of the keys. **/
public suspend inline fun AutoCompleteInteraction.suggestNumberMap(
    map: Map<String, Double>,
    strategy: FilterStrategy = FilterStrategy.Prefix
) {
    val option = focusedOption.value as? String
    var options = map

    if (option != null) {
        options = options.filterKeys { strategy.test(option, it) }
    }

    if (options.size > MAX_SUGGESTIONS) {
        options = options.entries.sortedBy { it.key }.take(MAX_SUGGESTIONS).associate { it.toPair() }
    }

    suggestNumber {
        options.forEach(::choice)
    }
}
