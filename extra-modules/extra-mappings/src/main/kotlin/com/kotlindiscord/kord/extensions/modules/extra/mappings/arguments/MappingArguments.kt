package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.modules.extra.mappings.converters.optionalMappingsVersion
import me.shedaniel.linkie.Namespace

/**
 * Arguments base for mapping commands.
 */
@Suppress("UndocumentedPublicProperty")
open class MappingArguments(val namespace: Namespace) : Arguments() {
    val query by string("query", "Name to query mappings for")

    val version by optionalMappingsVersion(
        "version",
        "Minecraft version to use for this query",
        true,
        namespace
    )

    open val mapDescriptors by defaultingBoolean(
        "mapDescriptors",
        "Whether to map descriptors to named instead of intermediary/hashed",
        true
    )
}
