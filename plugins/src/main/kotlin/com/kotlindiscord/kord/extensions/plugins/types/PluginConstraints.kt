package com.kotlindiscord.kord.extensions.plugins.types

import kotlinx.serialization.Serializable

@Serializable
public data class PluginConstraints(
	val conflicts: PluginConstraint = mapOf(),
	val needs: PluginConstraint = mapOf(),
	val wants: PluginConstraint = mapOf(),
)
