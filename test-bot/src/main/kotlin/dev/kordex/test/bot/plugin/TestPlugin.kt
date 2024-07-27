/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.test.bot.plugin

import dev.kordex.core.annotations.plugins.WiredPlugin
import dev.kordex.core.plugins.KordExPlugin
import dev.kordex.core.storage.StorageType
import dev.kordex.core.storage.StorageUnit

@WiredPlugin(
    TestPlugin.PLUGIN_ID,
	"0.0.1",
	"kord-extensions",
	"KordEx testing plugin",
	"MPL 2.0"
)
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
