/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins.test.core

import com.kotlindiscord.kord.extensions.plugins.Plugin
import io.github.oshai.kotlinlogging.KotlinLogging

public abstract class TestPlugin : Plugin<TestPlugin>() {
	private val logger = KotlinLogging.logger {}

	override fun internalLoad() {
		super.internalLoad()

		logger.info { "Plugin loaded: ${manifest.name}" }
	}

	override fun internalUnload() {
		super.internalUnload()

		logger.info { "Plugin unloaded: ${manifest.name}" }
	}
}
