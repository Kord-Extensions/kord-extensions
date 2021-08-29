package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalEnum
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.modules.extra.mappings.converters.optionalMappingsVersion
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.Channels
import me.shedaniel.linkie.namespaces.MojangNamespace

/** Arguments for Mojang mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class MojangArguments : Arguments() {
    val query by string("query", "Name to query mappings for")

    val channel by optionalEnum<Channels>(
        displayName = "channel",
        description = "Mappings channel to use for this query",
        typeName = "official/snapshot"
    )

    val version by optionalMappingsVersion(
        "version",
        "Minecraft version to use for this query",
        true,
        MojangNamespace
    )
}
