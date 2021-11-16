package com.kotlindiscord.kord.extensions.modules.extra.mappings.builders

import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.MappingsConfigAdapter
import com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.TomlMappingsConfig
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import me.shedaniel.linkie.Namespace

/** Builder used for configuring the mappings extension. **/
class ExtMappingsBuilder {
    /** Config adapter to use to load the mappings extension configuration. **/
    var config: MappingsConfigAdapter = TomlMappingsConfig()

    /** List of checks to apply against the name of the command. **/
    val commandChecks: MutableList<suspend (String) -> Check<ChatInputCommandInteractionCreateEvent>> = mutableListOf()

    /** List of checks to apply against the namespace corresponding with the command. **/
    val namespaceChecks: MutableList<suspend (Namespace) -> Check<ChatInputCommandInteractionCreateEvent>> = mutableListOf()

    /** Register a check that applies against the name of a command, and its message creation event. **/
    fun commandCheck(check: suspend (String) -> Check<ChatInputCommandInteractionCreateEvent>) {
        commandChecks.add(check)
    }

    /** Register a check that applies against the mappings namespace for a command, and its message creation event. **/
    fun namespaceCheck(check: suspend (Namespace) -> Check<ChatInputCommandInteractionCreateEvent>) {
        namespaceChecks.add(check)
    }
}
