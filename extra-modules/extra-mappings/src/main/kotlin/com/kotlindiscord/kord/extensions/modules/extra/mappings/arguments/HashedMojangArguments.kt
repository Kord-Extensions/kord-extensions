package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.optionalEnumChoice
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.Channels
import me.shedaniel.linkie.namespaces.MojangHashedNamespace

/** Arguments for hashed Mojang mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class HashedMojangArguments : MappingArguments(MojangHashedNamespace) {
    val channel by optionalEnumChoice<Channels>(
        displayName = "channel",
        description = "Mappings channel to use for this query",
        typeName = "official/snapshot"
    )
}
