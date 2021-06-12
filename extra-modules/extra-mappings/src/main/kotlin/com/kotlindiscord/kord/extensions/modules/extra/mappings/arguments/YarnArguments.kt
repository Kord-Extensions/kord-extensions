package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalEnum
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.modules.extra.mappings.converters.optionalMappingsVersion
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.YarnChannels
import me.shedaniel.linkie.namespaces.YarnNamespace

/** Arguments for Yarn mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class YarnArguments(patchworkEnabled: Boolean) : Arguments() {
    val query by string("query", "Name to query mappings for")

    val channel by optionalEnum<YarnChannels>(
        displayName = "channel",
        description = "Mappings channel to use for this query",

        typeName = "official/snapshot" + if (patchworkEnabled) {
            "/patchwork"
        } else {
            ""
        }
    )

    val version by optionalMappingsVersion(
        "version",
        "Minecraft version to use for this query",
        true,
        YarnNamespace
    )
}
