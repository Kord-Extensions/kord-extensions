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
