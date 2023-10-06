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
