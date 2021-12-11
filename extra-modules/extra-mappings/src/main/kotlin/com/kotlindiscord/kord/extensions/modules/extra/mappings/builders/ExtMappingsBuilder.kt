/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.mappings.builders

import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.MappingsConfigAdapter
import com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.TomlMappingsConfig
import me.shedaniel.linkie.Namespace
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent as SlashCommandInteractEvent

/** Builder used for configuring the mappings extension. **/
class ExtMappingsBuilder {
    /** Config adapter to use to load the mappings extension configuration. **/
    var config: MappingsConfigAdapter = TomlMappingsConfig()

    /** List of checks to apply against the name of the command. **/
    val commandChecks: MutableList<suspend (String) -> Check<SlashCommandInteractEvent>> = mutableListOf()

    /** List of checks to apply against the namespace corresponding with the command. **/
    val namespaceChecks: MutableList<suspend (Namespace) -> Check<SlashCommandInteractEvent>> = mutableListOf()

    /** Register a check that applies against the name of a command, and its message creation event. **/
    fun commandCheck(check: suspend (String) -> Check<SlashCommandInteractEvent>) {
        commandChecks.add(check)
    }

    /** Register a check that applies against the mappings namespace for a command, and its message creation event. **/
    fun namespaceCheck(check: suspend (Namespace) -> Check<SlashCommandInteractEvent>) {
        namespaceChecks.add(check)
    }
}
