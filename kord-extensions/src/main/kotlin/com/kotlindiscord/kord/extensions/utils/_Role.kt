/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import dev.kord.core.entity.Role

/**
 * Checks whether a [Role] can interact with another [Role] by comparing their [rawPosition]s.
 *
 * Throws an [IllegalArgumentException] when the roles are not from the same guild.
 */
public fun Role.canInteract(role: Role): Boolean {
	if (role.guildId != guildId) {
		throw IllegalArgumentException("canInteract can only be called within the same guild!")
	}

	return role.rawPosition < rawPosition
}
