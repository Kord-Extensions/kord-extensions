/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.modules.func.mappings.builders.ExtMappingsBuilder

/**
 * Configure the mappings extension and add it to the bot.
 */
fun ExtensibleBotBuilder.ExtensionsBuilder.extMappings(builder: ExtMappingsBuilder.() -> Unit) {
	val obj = ExtMappingsBuilder()

	builder(obj)
	MappingsExtension.configure(obj)

	add(::MappingsExtension)
}
