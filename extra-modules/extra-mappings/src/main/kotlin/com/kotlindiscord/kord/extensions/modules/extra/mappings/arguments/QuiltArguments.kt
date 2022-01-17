package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.optionalEnumChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.Channels
import me.shedaniel.linkie.namespaces.QuiltMappingsNamespace

/**
 * Arguments for Quilt mapping lookup commands.
 */
@Suppress("UndocumentedPublicProperty")
class QuiltArguments : MappingWithChannelArguments(QuiltMappingsNamespace), IntermediaryMappable {
    override val channel by optionalEnumChoice<Channels> {
        name = "channel"
        description = "Mappings channel to use for this query"

        typeName = "official/snapshot"
    }

    override val mapDescriptors by defaultingBoolean {
        name = "map-descriptor"
        description = "Whether to map field/method descriptors to named instead of hashed"
        defaultValue = true
    }
}
