/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
