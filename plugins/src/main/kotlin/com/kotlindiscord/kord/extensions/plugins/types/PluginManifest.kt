/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins.types

import io.github.z4kn4fein.semver.Version
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Suppress("DataClassShouldBeImmutable")
public data class PluginManifest(
	@SerialName("class")
	val classRef: String,

	val id: String,

	val description: String,
	val license: String,
	val name: String,
	val version: Version,

	val constraints: PluginConstraints = PluginConstraints(),
) {
	@Transient
	lateinit var jarPath: String

	@Transient
	public var failed: Boolean = false
}
