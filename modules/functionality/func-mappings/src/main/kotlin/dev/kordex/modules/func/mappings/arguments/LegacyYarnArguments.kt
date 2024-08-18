/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.mappings.arguments

import dev.kordex.core.commands.converters.impl.defaultingBoolean
import me.shedaniel.linkie.namespaces.LegacyYarnNamespace

/** Arguments for Legacy Yarn mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class LegacyYarnArguments : MappingArguments(LegacyYarnNamespace), IntermediaryMappable {
	override val mapDescriptors by defaultingBoolean {
		name = "map-descriptor"
		description = "Whether to map field/method descriptors to named instead of intermediary/hashed"
		defaultValue = true
	}
}
