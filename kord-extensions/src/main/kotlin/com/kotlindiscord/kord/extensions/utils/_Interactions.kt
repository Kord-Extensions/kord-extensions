/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import dev.kord.core.behavior.interaction.suggestInteger
import dev.kord.core.behavior.interaction.suggestNumber
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
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

/** Use a map to populate an autocomplete interaction, filtering as described by the provided [strategy]. **/
public suspend inline fun AutoCompleteInteraction.suggestStringMap(
    map: Map<String, String>,
    strategy: FilterStrategy = FilterStrategy.Prefix,
    suggestInputWithoutMatches: Boolean = false,
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
        if (suggestInputWithoutMatches && options.isEmpty() && !option.isNullOrEmpty()) {
            choice(option, option)
        } else {
            options.forEach(::choice)
        }
    }
}

/**
 * Use a collection (like a list) to populate an autocomplete interaction, filtering as described by the provided
 * [strategy].
 */
public suspend inline fun AutoCompleteInteraction.suggestStringCollection(
    collection: Collection<String>,
    strategy: FilterStrategy = FilterStrategy.Prefix,
) {
    suggestStringMap(
        collection.associateBy { it },
        strategy
    )
}

/** Use a map to populate an autocomplete interaction, filtering as described by the provided [strategy]. **/
public suspend inline fun AutoCompleteInteraction.suggestIntMap(
    map: Map<String, Int>,
    strategy: FilterStrategy = FilterStrategy.Prefix,
) {
    suggestLongMap(map.mapValues { it.value.toLong() }, strategy)
}

/**
 * Use a collection (like a list) to populate an autocomplete interaction, filtering as described by the provided
 * [strategy].
 */
public suspend inline fun AutoCompleteInteraction.suggestIntCollection(
    collection: Collection<Int>,
    strategy: FilterStrategy = FilterStrategy.Prefix,
) {
    suggestIntMap(
        collection.associateBy { it.toString() },
        strategy
    )
}

/** Use a map to populate an autocomplete interaction, filtering as described by the provided [strategy]. **/
public suspend inline fun AutoCompleteInteraction.suggestLongMap(
    map: Map<String, Long>,
    strategy: FilterStrategy = FilterStrategy.Prefix,
    suggestInputWithoutMatches: Boolean = false,
) {
    val option = focusedOption.value as? String
    var options = map

    if (option != null) {
        options = options.filterKeys { strategy.test(option, it) }
    }

    if (options.size > MAX_SUGGESTIONS) {
        options = options.entries.sortedBy { it.key }.take(MAX_SUGGESTIONS).associate { it.toPair() }
    }

    suggestInteger {
        if (suggestInputWithoutMatches && options.isEmpty() && !option.isNullOrEmpty()) {
            val longValue = option.toLongOrNull()

            if (longValue != null) {
                choice(option, longValue)
            }
        } else {
            options.forEach(::choice)
        }
    }
}

/**
 * Use a collection (like a list) to populate an autocomplete interaction, filtering as described by the provided
 * [strategy].
 */
public suspend inline fun AutoCompleteInteraction.suggestLongCollection(
    collection: Collection<Long>,
    strategy: FilterStrategy = FilterStrategy.Prefix,
) {
    suggestLongMap(
        collection.associateBy { it.toString() },
        strategy
    )
}

/** Use a map to populate an autocomplete interaction, filtering as described by the provided [strategy]. **/
public suspend inline fun AutoCompleteInteraction.suggestDoubleMap(
    map: Map<String, Double>,
    strategy: FilterStrategy = FilterStrategy.Prefix,
) {
    suggestNumberMap(map, strategy)
}

/**
 * Use a collection (like a list) to populate an autocomplete interaction, filtering as described by the provided
 * [strategy].
 */
public suspend inline fun AutoCompleteInteraction.suggestDoubleCollection(
    collection: Collection<Double>,
    strategy: FilterStrategy = FilterStrategy.Prefix,
) {
    suggestDoubleMap(
        collection.associateBy { it.toString() },
        strategy
    )
}

/** Use a map to populate an autocomplete interaction, filtering as described by the provided [strategy]. **/
public suspend inline fun AutoCompleteInteraction.suggestNumberMap(
    map: Map<String, Double>,
    strategy: FilterStrategy = FilterStrategy.Prefix,
    suggestInputWithoutMatches: Boolean = false,
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
        if (suggestInputWithoutMatches && options.isEmpty() && !option.isNullOrEmpty()) {
            val doubleValue = option.toDoubleOrNull()

            if (doubleValue != null) {
                choice(option, doubleValue)
            }
        } else {
            options.forEach(::choice)
        }
    }
}

/**
 * Use a collection (like a list) to populate an autocomplete interaction, filtering as described by the provided
 * [strategy].
 */
public suspend inline fun AutoCompleteInteraction.suggestNumberCollection(
    collection: Collection<Double>,
    strategy: FilterStrategy = FilterStrategy.Prefix,
) {
    suggestNumberMap(
        collection.associateBy { it.toString() },
        strategy
    )
}
