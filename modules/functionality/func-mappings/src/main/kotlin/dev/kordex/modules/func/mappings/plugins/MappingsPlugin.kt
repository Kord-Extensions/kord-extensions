/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.mappings.plugins

import dev.kordex.core.plugins.KordExPlugin
import dev.kordex.modules.func.mappings.MappingsExtension
import dev.kordex.modules.func.mappings.builders.ExtMappingsBuilder

// @WiredPlugin(
// 	MappingsPlugin.PLUGIN_ID,
// 	"0.0.1",
// 	"kord-extensions",
// 	"KordEx Minecraft Mappings plugin",
// 	"MPL 2.0"
// )

/**
 * Class representing a simplified mappings plugin. No configuration supported yet.
 *
 * TODO: Switch to the official Gradle plugin for development, or find some other way to package this.
 **/
class MappingsPlugin : KordExPlugin() {
	override suspend fun setup() {
		MappingsExtension.configure(ExtMappingsBuilder())
		extension(::MappingsExtension)
	}

	companion object {
		/** This plugin's ID, provided here for re-use. **/
		const val PLUGIN_ID: String = "ext-mappings"
	}
}
