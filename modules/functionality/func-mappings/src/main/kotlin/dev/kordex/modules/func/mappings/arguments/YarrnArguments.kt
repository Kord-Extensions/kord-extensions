/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings.arguments

import dev.kordex.core.commands.converters.impl.defaultingBoolean
import me.shedaniel.linkie.namespaces.YarrnNamespace

/** Arguments for Yarrn (Yarn for Infdev) mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class YarrnArguments : MappingArguments(YarrnNamespace), IntermediaryMappable {
	override val mapDescriptors by defaultingBoolean {
		name = "map-descriptor"
		description = "Whether to map field/method descriptors to named instead of intermediary/hashed"
		defaultValue = true
	}
}
