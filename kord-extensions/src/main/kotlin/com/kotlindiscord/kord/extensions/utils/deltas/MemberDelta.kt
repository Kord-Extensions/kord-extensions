package com.kotlindiscord.kord.extensions.utils.deltas

import com.gitlab.kordlib.common.entity.Premium
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.common.entity.UserFlags
import com.gitlab.kordlib.core.entity.Member
import java.time.Instant
import java.util.*

/**
 * Represents the difference between two Kord [Member] objects. This includes everything from [UserDelta].
 *
 * This is intended for use with events that change things, to make logging easier - but may have other applications.
 *
 * @param avatar The new object's avatar URL, or null if there's no difference.
 * @param username The new object's username, or null if there's no difference.
 * @param discriminator The new object's discriminator, or null if there's no difference.
 * @param flags The new object's user flags, or null if there's no difference.
 * @param nitro The new object's nitro status, or null if there's no difference.
 *
 * @param nickname The new object's nickname. This is an [Optional] that will be absent if there's no difference.
 * @param boosting The new object's boosting status. This is an [Optional] that will be absent if there's no difference.
 * @param roles The new object's role IDs, or null if there's no difference.
 * @param owner The new object's server owner status, or null if there's no difference.
 */
class MemberDelta(
    avatar: String?,
    username: String?,
    discriminator: String?,
    flags: UserFlags?,
    nitro: Premium?,

    val nickname: Optional<String>?,
    val boosting: Optional<Instant>?,
    val roles: Set<Snowflake>?,
    val owner: Boolean?
) : UserDelta(avatar, username, discriminator, flags, nitro) {
    /**
     * A Set representing the values that have changes. Each value is represented by a human-readable string.
     */
    override val changes: Set<String> by lazy {
        val s = super.changes.toMutableSet()

        if (nickname != null) s.add("nickname")
        if (boosting != null) s.add("boosting")
        if (roles != null) s.add("roles")
        if (owner != null) s.add("owner")

        s
    }

    companion object {
        /**
         * Given an old and new [Member] object, return a [MemberDelta] representing the changes between them.
         *
         * @param old The older [Member] object.
         * @param new The newer [Member] object.
         */
        suspend fun from(old: Member?, new: Member): MemberDelta? {
            old ?: return null

            val user = UserDelta.from(old, new) ?: return null

            return MemberDelta(
                user.avatar,
                user.username,
                user.discriminator,
                user.flags,
                user.nitro,

                if (old.nickname != new.nickname) {
                    Optional.ofNullable(new.nickname)
                } else {
                    null
                },

                if (old.premiumSince != new.premiumSince) {
                    Optional.ofNullable(new.premiumSince)
                } else {
                    null
                },

                if (old.roleIds != new.roleIds) new.roleIds else null,
                if (old.isOwner() != new.isOwner()) new.isOwner() else null
            )
        }
    }
}
