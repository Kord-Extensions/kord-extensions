/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins.extra

import com.kotlindiscord.kord.extensions.modules.extra.mappings.MappingsExtension
import com.kotlindiscord.kord.extensions.modules.extra.mappings.builders.ExtMappingsBuilder
import com.kotlindiscord.kord.extensions.plugins.KordExPlugin
import com.kotlindiscord.kord.extensions.plugins.annotations.plugins.WiredPlugin
import org.pf4j.PluginWrapper

/** Class representing a simplified mappings plugin. No configuration supported yet. **/
@WiredPlugin(
	MappingsPlugin.PLUGIN_ID,
	"0.0.1",
	"kord-extensions",
	"KordEx Minecraft Mappings plugin",
	"MPL 2.0"
)
class MappingsPlugin(wrapper: PluginWrapper) : KordExPlugin(wrapper) {
	override suspend fun setup() {
		MappingsExtension.configure(ExtMappingsBuilder())
		extension(::MappingsExtension)
	}

	companion object {
		/** This plugin's ID, provided here for re-use. **/
		const val PLUGIN_ID: String = "ext-mappings"
	}
}
