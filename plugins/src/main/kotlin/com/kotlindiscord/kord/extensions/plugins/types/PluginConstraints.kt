/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins.types

import kotlinx.serialization.Serializable

@Serializable
public data class PluginConstraints(
	val conflicts: PluginConstraint = mapOf(),
	val needs: PluginConstraint = mapOf(),
	val wants: PluginConstraint = mapOf(),
)
