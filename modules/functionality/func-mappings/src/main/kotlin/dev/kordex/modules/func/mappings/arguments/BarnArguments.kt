/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings.arguments

import dev.kordex.core.commands.converters.impl.defaultingBoolean
import me.shedaniel.linkie.namespaces.BarnNamespace

/** Arguments for Barn mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class BarnArguments : MappingArguments(BarnNamespace), IntermediaryMappable {
	override val mapDescriptors by defaultingBoolean {
		name = "map-descriptor"
		description = "Whether to map field/method descriptors to named instead of intermediary"
		defaultValue = true
	}
}
