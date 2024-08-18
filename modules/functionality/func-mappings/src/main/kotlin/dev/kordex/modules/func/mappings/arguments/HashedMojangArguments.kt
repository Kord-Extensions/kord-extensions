/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.mappings.arguments

import dev.kordex.core.commands.application.slash.converters.impl.optionalEnumChoice
import dev.kordex.core.commands.converters.impl.defaultingBoolean
import dev.kordex.modules.func.mappings.enums.Channels
import me.shedaniel.linkie.namespaces.MojangHashedNamespace

/** Arguments for hashed Mojang mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class HashedMojangArguments : MappingWithChannelArguments(MojangHashedNamespace), IntermediaryMappable {
	override val channel by optionalEnumChoice<Channels> {
		name = "channel"
		description = "Mappings channel to use for this query"
		typeName = "official/snapshot"
	}

	override val mapDescriptors by defaultingBoolean {
		name = "map-descriptor"
		description = "Whether to map field/method descriptors to named instead of intermediary/hashed"
		defaultValue = true
	}
}
