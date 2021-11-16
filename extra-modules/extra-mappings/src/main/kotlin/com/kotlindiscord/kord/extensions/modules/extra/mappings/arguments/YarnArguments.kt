package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.optionalEnumChoice
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.YarnChannels
import me.shedaniel.linkie.namespaces.YarnNamespace

/** Arguments for Yarn mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class YarnArguments(patchworkEnabled: Boolean) : MappingArguments(YarnNamespace) {
    val channel by optionalEnumChoice<YarnChannels>(
        displayName = "channel",
        description = "Mappings channel to use for this query",

        typeName = "official/snapshot" + if (patchworkEnabled) {
            "/patchwork"
        } else {
            ""
        },
        validator = { _, value ->
            if (value == YarnChannels.PATCHWORK && !patchworkEnabled) {
                throw IllegalArgumentException("Patchwork channel is not available on this server.")
            }
        }
    )
}
