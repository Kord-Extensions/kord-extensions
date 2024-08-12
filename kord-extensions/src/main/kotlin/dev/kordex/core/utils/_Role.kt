/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils

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
