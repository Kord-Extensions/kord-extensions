/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.plugins

import org.pf4j.*
import org.pf4j.PluginManager
import java.nio.file.Path

/** Default plugin loader, with a changed classloader. **/
public class DefaultLoader(pluginManager: PluginManager?) : DefaultPluginLoader(pluginManager) {
	override fun loadPlugin(pluginPath: Path?, pluginDescriptor: PluginDescriptor?): ClassLoader {
		val pluginClassLoader = PluginClassLoader(
			pluginManager,
			pluginDescriptor,
			ClassLoader.getSystemClassLoader(),
			ClassLoadingStrategy.APD
		)

		pluginClassLoader.addFile(pluginPath!!.toFile())

		return pluginClassLoader
	}
}
