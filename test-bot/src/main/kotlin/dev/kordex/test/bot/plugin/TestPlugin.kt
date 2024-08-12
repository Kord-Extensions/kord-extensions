/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.bot.plugin

import dev.kordex.core.plugins.KordExPlugin
import dev.kordex.core.storage.StorageType
import dev.kordex.core.storage.StorageUnit

// @WiredPlugin(
//    TestPlugin.PLUGIN_ID,
// 	"0.0.1",
// 	"kord-extensions",
// 	"KordEx testing plugin",
// 	"EUPL-1.2"
// )

/**
 * Class representing a test plugin.
 *
 * TODO: Switch to the official Gradle plugin for development, or find some other way to package this.
 **/
public class TestPlugin : KordExPlugin() {
	override suspend fun setup() {
		extension(::TestPluginExtension)
	}

	public companion object {
		public const val PLUGIN_ID: String = "test-plugin"

		internal val DATA_UNIT = StorageUnit(
			StorageType.Data,
			PLUGIN_ID,
			"test",
			TestPluginData::class
		)
	}
}
