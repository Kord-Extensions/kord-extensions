package com.kotlindiscord.kord.extensions.utils.deltas

import dev.kord.common.entity.UserFlags
import dev.kord.core.entity.User

/**
 * Represents the difference between two Kord [User] objects.
 *
 * This is intended for use with events that change things, to make logging easier - but may have other applications.
 *
 * @param avatar The new object's avatar URL, or null if there's no difference.
 * @param username The new object's username, or null if there's no difference.
 * @param discriminator The new object's discriminator, or null if there's no difference.
 * @param flags The new object's user flags, or null if there's no difference.
 */
public open class UserDelta(
    public val avatar: String?,
    public val username: String?,
    public val discriminator: String?,
    public val flags: UserFlags?
) {
    /**
     * A Set representing the values that have changes. Each value is represented by a human-readable string.
     */
    public open val changes: Set<String> by lazy {
        val s = mutableSetOf<String>()

        if (avatar != null) s.add("avatar")
        if (username != null) s.add("username")
        if (discriminator != null) s.add("discriminator")
        if (flags != null) s.add("flags")

        s
    }

    public companion object {
        /**
         * Given an old and new [User] object, return a [UserDelta] representing the changes between them.
         *
         * @param old The older [User] object.
         * @param new The newer [User] object.
         */
        public fun from(old: User?, new: User): UserDelta? {
            old ?: return null

            return UserDelta(
                if (old.avatar.url != new.avatar.url) new.avatar.url else null,
                if (old.username != new.username) new.username else null,
                if (old.discriminator != new.discriminator) new.discriminator else null,
                if (old.publicFlags != new.publicFlags) new.publicFlags else null
            )
        }
    }
}
