/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings.utils

import me.shedaniel.linkie.namespaces.YarnNamespace
import me.shedaniel.linkie.utils.tryToVersion

/**
 * A wrapper to find the latest release and snapshot
 * of what [YarnNamespace] specifies. This is used
 * because of Linkie's removal of the ability
 * to specify different default versions
 * for different "channels".
 */
object YarnReleaseContainer {
	/**
	 * The latest Minecraft release to have yarn mappings built for it.
	 */
	val latestRelease = YarnNamespace.latestYarnVersion

	/**
	 * The latest Minecraft snapshot release to have yarn mappings built for it.
	 * This is generated in a similar fashion to [YarnNamespace.latestYarnVersion]
	 * since Linkie removed channel support.
	 */
	val latestSnapshot = YarnNamespace.yarnBuilds.keys
		.mapNotNull { it.tryToVersion() }.maxOrNull()?.toString()
}
