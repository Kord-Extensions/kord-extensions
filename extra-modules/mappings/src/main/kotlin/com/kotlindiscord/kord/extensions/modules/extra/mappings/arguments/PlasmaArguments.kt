package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.modules.extra.mappings.converters.optionalMappingsVersion
import me.shedaniel.linkie.namespaces.PlasmaNamespace

/** Arguments for Mojang mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class PlasmaArguments : Arguments() {
    val query by string("query", "Name to query mappings for")

    val version by optionalMappingsVersion(
        "version",
        "Minecraft version to use for this query",
        true,
        PlasmaNamespace
    )
}
