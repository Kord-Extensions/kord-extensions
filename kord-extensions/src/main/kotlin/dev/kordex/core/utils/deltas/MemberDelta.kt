/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils.deltas

import dev.kord.common.entity.UserFlags
import dev.kord.common.entity.optional.Optional
import dev.kord.core.entity.Asset
import dev.kord.core.entity.Member
import kotlinx.datetime.Instant
import kotlin.contracts.contract

/**
 * Represents the difference between two Kord [Member] objects. This includes everything from [UserDelta].
 *
 * This is intended for use with events that change things, to make logging easier - but may have other applications.
 * All properties available on this object have the same names as the corresponding properties on the [Member] object.
 *
 * Optionals will be [Optional.Missing] if there was no change - otherwise they'll contain the value from the `new`
 * [Member].
 */
@Suppress("UndocumentedPublicProperty")
public class MemberDelta(
	avatar: Optional<Asset?>,
	username: Optional<String>,
	discriminator: Optional<String>,
	flags: Optional<UserFlags?>,

	public val nickname: Optional<String?>,
	public val boosting: Optional<Instant?>,
	public val roles: Optional<RoleDelta>,
	public val owner: Optional<Boolean>,
	public val pending: Optional<Boolean>,
) : UserDelta(avatar, username, discriminator, flags) {
	/**
	 * A Set representing the values that have changes. Each value is represented by a human-readable string.
	 */
	override val changes: Set<String> by lazy {
		super.changes.toMutableSet().apply {
			if (nickname !is Optional.Missing) add("nickname")
			if (boosting !is Optional.Missing) add("boosting")
			if (roles !is Optional.Missing) add("roles")
			if (owner !is Optional.Missing) add("owner")
			if (pending !is Optional.Missing) add("pending")
		}
	}

	public companion object {
		/**
		 * Given an old and new [Member] object, return a [MemberDelta] representing the changes between them.
		 *
		 * @param old The older [Member] object.
		 * @param new The newer [Member] object.
		 */
		public suspend fun from(old: Member?, new: Member): MemberDelta? {
			contract {
				returns(null) implies (old == null)
			}

			old ?: return null

			val user = UserDelta.from(old, new) ?: return null
			val roleDelta = RoleDelta.from(old, new)

			return MemberDelta(
				user.avatar,
				user.username,
				user.discriminator,
				user.flags,

				if (old.nickname != new.nickname) Optional(new.nickname) else Optional.Missing(),
				if (old.premiumSince != new.premiumSince) Optional(new.premiumSince) else Optional.Missing(),
				if (roleDelta != null) Optional(roleDelta) else Optional.Missing(),
				if (old.isOwner() != new.isOwner()) Optional(new.isOwner()) else Optional.Missing(),
				if (old.isPending != new.isPending) Optional(new.isPending) else Optional.Missing()
			)
		}
	}
}
