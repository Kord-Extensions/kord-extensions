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
