/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.plugins

import dev.kordex.core.KORDEX_VERSION
import org.pf4j.*
import java.nio.file.Path

@Suppress("SpreadOperator")
/** Module manager, in charge of loading and managing module "plugins". **/
public open class PluginManager(roots: List<Path>) : DefaultPluginManager(*roots.toTypedArray()) {
	init {
		systemVersion = KORDEX_VERSION
	}

	override fun createPluginDescriptorFinder(): PluginDescriptorFinder =
		PropertiesPluginDescriptorFinder()

	override fun createPluginLoader(): PluginLoader? {
		return CompoundPluginLoader()
			.add(DevelopmentPluginLoader(this)) { this.isDevelopment }
			.add(JarPluginLoader(this)) { this.isNotDevelopment }
			.add(DefaultPluginLoader(this)) { this.isNotDevelopment }
	}
}
