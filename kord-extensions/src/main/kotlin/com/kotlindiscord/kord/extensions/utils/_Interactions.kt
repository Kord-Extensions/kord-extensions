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
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent

/** Retrieve the option that's currently focused in the client. **/
public val AutoCompleteInteractionCreateEvent.focusedOption: OptionValue<*>
    get() = this.interaction.command.options.values.first { it.focused }

/** Retrieve the option that's currently focused in the client. **/
public val AutoCompleteInteraction.focusedOption: OptionValue<*>
    get() = this.command.options.values.first { it.focused }

/** Use a map to populate an autocomplete interaction, filtering by comparing the input with the start of the keys. **/
public suspend inline fun AutoCompleteInteraction.suggestStringMap(map: Map<String, String>) {
    val option = focusedOption.value as? String
    var options = map

    if (option != null) {
        options = map.filterKeys { it.lowercase().startsWith(option.lowercase()) }
    }

    suggestString {
        options.forEach(::choice)
    }
}

/** Use a map to populate an autocomplete interaction, filtering by comparing the input with the start of the keys. **/
public suspend inline fun AutoCompleteInteraction.suggestIntMap(map: Map<String, Int>) {
    suggestLongMap(map.mapValues { it.value.toLong() })
}

/** Use a map to populate an autocomplete interaction, filtering by comparing the input with the start of the keys. **/
public suspend inline fun AutoCompleteInteraction.suggestLongMap(map: Map<String, Long>) {
    val option = focusedOption.value as? String
    var options = map

    if (option != null) {
        options = map.filterKeys { it.lowercase().startsWith(option.lowercase()) }
    }

    suggestInt {
        options.forEach(::choice)
    }
}

/** Use a map to populate an autocomplete interaction, filtering by comparing the input with the start of the keys. **/
public suspend inline fun AutoCompleteInteraction.suggestNumberMap(map: Map<String, Double>) {
    val option = focusedOption.value as? String
    var options = map

    if (option != null) {
        options = map.filterKeys { it.lowercase().startsWith(option.lowercase()) }
    }

    suggestNumber {
        options.forEach(::choice)
    }
}
