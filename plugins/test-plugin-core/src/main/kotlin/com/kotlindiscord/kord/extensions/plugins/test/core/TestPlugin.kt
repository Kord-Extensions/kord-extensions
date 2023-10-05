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
