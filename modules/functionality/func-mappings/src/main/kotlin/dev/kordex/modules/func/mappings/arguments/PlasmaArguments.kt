/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.mappings.arguments

import dev.kordex.core.commands.converters.impl.defaultingBoolean
import dev.kordex.modules.func.mappings.i18n.generated.MappingsTranslations
import me.shedaniel.linkie.namespaces.PlasmaNamespace

/** Arguments for Plasma mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class PlasmaArguments : MappingArguments(PlasmaNamespace), IntermediaryMappable {
	override val mapDescriptors by defaultingBoolean {
		name = MappingsTranslations.Argument.MapDescriptor.name
		description = MappingsTranslations.Argument.MapDescriptor.description
		defaultValue = true
	}
}
