/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings.utils

import me.shedaniel.linkie.namespaces.MojangNamespace
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

/** Mojang release container, allowing for retrieval of various Mojang mappings release versions. **/
object MojangReleaseContainer {
	/**
	 * A wrapper for [MojangNamespace.latestRelease], a private field.
	 */
	var latestRelease: String
		get() = latestReleaseProperty.getter.call()
		set(value) = latestReleaseProperty.setter.call(value)

	/**
	 * A wrapper for [MojangNamespace.latestSnapshot], a private field.
	 */
	var latestSnapshot: String
		get() = latestSnapshotProperty.getter.call()
		set(value) = latestSnapshotProperty.setter.call(value)

	@Suppress("UNCHECKED_CAST")
	private val latestSnapshotProperty by lazy {
		MojangNamespace::class.declaredMemberProperties
			.first { it.name == "latestSnapshot" }
			.also { it.isAccessible = true } as KMutableProperty1<MojangNamespace, String>
	}

	@Suppress("UNCHECKED_CAST")
	private val latestReleaseProperty by lazy {
		MojangNamespace::class.declaredMemberProperties
			.first { it.name == "latestRelease" }
			.also { it.isAccessible = true } as KMutableProperty1<MojangNamespace, String>
	}
}
