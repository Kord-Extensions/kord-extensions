/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins.test.two

import com.kotlindiscord.kord.extensions.plugins.test.core.TestPlugin
import io.github.oshai.kotlinlogging.KotlinLogging

public class TestPluginTwo : TestPlugin() {
	private val logger = KotlinLogging.logger { }

	override fun load() {
		logger.info { "Plugin 2 loaded" }
	}

	override fun unload() {
		logger.info { "Plugin 2 unloaded" }
	}
}
