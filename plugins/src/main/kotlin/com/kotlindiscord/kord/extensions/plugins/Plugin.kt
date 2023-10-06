package com.kotlindiscord.kord.extensions.plugins

import com.kotlindiscord.kord.extensions.plugins.types.PluginManifest

public abstract class Plugin<T : Plugin<T>> {
	public lateinit var pluginManager: PluginManager<T>
		internal set

	public lateinit var manifest: PluginManifest
		internal set

	public abstract fun load()
	public abstract fun unload()

	public open fun onPluginLoaded(plugin: T) {}
	public open fun onPluginUnloaded(plugin: T) {}

	public open fun internalLoad() {
		load()
	}

	public open fun internalUnload() {
		unload()
	}
}
