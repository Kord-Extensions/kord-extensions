/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
