package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalEnum
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.YarnChannels
import me.shedaniel.linkie.namespaces.YarnNamespace

/** Arguments for Yarn mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class YarnArguments(patchworkEnabled: Boolean) : MappingArguments(YarnNamespace) {
    val channel by optionalEnum<YarnChannels>(
        displayName = "channel",
        description = "Mappings channel to use for this query",

        typeName = "official/snapshot" + if (patchworkEnabled) {
            "/patchwork"
        } else {
            ""
        }
    )
}
