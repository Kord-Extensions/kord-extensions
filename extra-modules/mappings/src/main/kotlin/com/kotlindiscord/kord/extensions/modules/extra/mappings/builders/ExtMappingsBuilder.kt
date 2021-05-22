package com.kotlindiscord.kord.extensions.modules.extra.mappings.builders

import com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.MappingsConfigAdapter
import com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.TomlMappingsConfig
import dev.kord.core.event.message.MessageCreateEvent
import me.shedaniel.linkie.Namespace

/** Builder used for configuring the mappings extension. **/
class ExtMappingsBuilder {
    /** Config adapter to use to load the mappings extension configuration. **/
    var config: MappingsConfigAdapter = TomlMappingsConfig()

    /** List of checks to apply against the name of the command. **/
    val commandChecks: MutableList<suspend (String) -> (suspend (MessageCreateEvent) -> Boolean)> = mutableListOf()

    /** List of checks to apply against the namespace corresponding with the command. **/
    val namespaceChecks: MutableList<suspend (Namespace) -> (suspend (MessageCreateEvent) -> Boolean)> = mutableListOf()

    /** Register a check that applies against the name of a command, and its message creation event. **/
    fun commandCheck(check: suspend (String) -> (suspend (MessageCreateEvent) -> Boolean)) {
        commandChecks.add(check)
    }

    /** Register a check that applies against the mappings namespace for a command, and its message creation event. **/
    fun namespaceCheck(check: suspend (Namespace) -> (suspend (MessageCreateEvent) -> Boolean)) {
        namespaceChecks.add(check)
    }
}
