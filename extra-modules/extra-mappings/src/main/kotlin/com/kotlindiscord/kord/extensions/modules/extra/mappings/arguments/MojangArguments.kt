package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalEnum
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.Channels
import me.shedaniel.linkie.namespaces.MojangNamespace

/** Arguments for Mojang mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class MojangArguments : MappingArguments(MojangNamespace) {
    val channel by optionalEnum<Channels>(
        displayName = "channel",
        description = "Mappings channel to use for this query",
        typeName = "official/snapshot"
    )
}
