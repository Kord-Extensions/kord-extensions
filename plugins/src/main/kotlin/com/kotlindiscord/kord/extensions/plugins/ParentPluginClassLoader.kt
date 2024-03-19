/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins

public class ParentPluginClassLoader(
	public val pluginsDirectory: String,
) : ClassLoader() {
	private val childClassLoaders: MutableMap<String, PluginClassLoader> = mutableMapOf()

	public fun loadPluginLoader(pluginId: String, jarPath: String): Boolean {
		if (pluginId in childClassLoaders) {
			return false
		}

		childClassLoaders[pluginId] = PluginClassLoader(jarPath, this)

		return true
	}

	public fun removePluginLoader(pluginId: String): Boolean =
		childClassLoaders.remove(pluginId) != null

	override fun loadClass(name: String?): Class<*> {
		for (cl in childClassLoaders.values) {
			try {
				return cl.loadClassWithoutRecursion(name)
			} catch (_: ClassNotFoundException) {
			}
		}

		return getSystemClassLoader().loadClass(name)
	}
}
