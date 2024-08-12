/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils.deltas

import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import kotlinx.coroutines.flow.toSet
import kotlin.contracts.contract

/**
 * Represents the difference in roles between two Kord [Member] objects.
 *
 * This is intended for use with [MemberDelta], and is included as the `roles` attribute.
 */
@Suppress("UndocumentedPublicProperty")
public data class RoleDelta(
	private val old: Set<Role>,
	private val new: Set<Role>,
) {
	/** A set representing any roles that were added in [new], if any. **/
	public val added: Set<Role> = new - old

	/** A set representing any roles that removed in [new], if any. **/
	public val removed: Set<Role> = old - new

	public companion object {
		/**
		 * Given an old and new Kord [Member] object, return a [RoleDelta] representing the changes between them.
		 *
		 * This function will return `null` if there were no changes, or if [old] is `null`.
		 *
		 * @param old The older [Member] object.
		 * @param new The newer [Member] object.
		 */
		public suspend fun from(old: Member?, new: Member): RoleDelta? {
			contract {
				returns(null) implies (old == null)
			}

			old ?: return null

			if (old.roleIds == new.roleIds) return null

			return RoleDelta(old.roles.toSet(), new.roles.toSet())
		}
	}
}
