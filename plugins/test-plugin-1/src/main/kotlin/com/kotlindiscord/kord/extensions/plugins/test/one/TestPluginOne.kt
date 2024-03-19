/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins.test.one

import com.kotlindiscord.kord.extensions.plugins.test.core.TestPlugin
import io.github.oshai.kotlinlogging.KotlinLogging

public class TestPluginOne : TestPlugin() {
	private val logger = KotlinLogging.logger { }

	override fun load() {
		logger.info { "Plugin 1 loaded" }
	}

	override fun unload() {
		logger.info { "Plugin 1 unloaded" }
	}
}
