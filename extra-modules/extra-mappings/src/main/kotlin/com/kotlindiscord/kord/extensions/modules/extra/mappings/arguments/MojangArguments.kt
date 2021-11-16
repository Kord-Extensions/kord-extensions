package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.optionalEnumChoice
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.Channels
import me.shedaniel.linkie.namespaces.MojangNamespace

/** Arguments for Mojang mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class MojangArguments : MappingArguments(MojangNamespace) {
    val channel by optionalEnumChoice<Channels>(
        displayName = "channel",
        description = "Mappings channel to use for this query",
        typeName = "official/snapshot"
    )
}
