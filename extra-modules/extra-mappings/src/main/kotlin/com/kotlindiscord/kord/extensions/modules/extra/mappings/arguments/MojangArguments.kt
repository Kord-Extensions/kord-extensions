package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.converters.optionalEnum
import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.modules.extra.mappings.converters.optionalMappingsVersion
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.Channels
import me.shedaniel.linkie.namespaces.MojangNamespace

/** Arguments for Mojang mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class MojangArguments : Arguments() {
    val query by string("query", "Name to query mappings for")

    val channel by optionalEnum<Channels>(
        "channel",
        "Mappings channel to use for this query",
        "official/snapshot"
    )

    val version by optionalMappingsVersion(
        "version",
        "Minecraft version to use for this query",
        true,
        MojangNamespace
    )
}
