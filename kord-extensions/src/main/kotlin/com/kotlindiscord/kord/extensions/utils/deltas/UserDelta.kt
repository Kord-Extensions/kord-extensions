package com.kotlindiscord.kord.extensions.utils.deltas

import dev.kord.common.entity.UserFlags
import dev.kord.common.entity.optional.Optional
import dev.kord.core.entity.Icon
import dev.kord.core.entity.User
import kotlin.contracts.contract

/**
 * Represents the difference between two Kord [User] objects.
 *
 * This is intended for use with events that change things, to make logging easier - but may have other applications.
 * All properties available on this object have the same names as the corresponding properties on the [User] object.
 *
 * Optionals will be [Optional.Missing] if there was no change - otherwise they'll contain the value from the `new`
 * [User].
 */
@Suppress("UndocumentedPublicProperty")
public open class UserDelta(
    public val avatar: Optional<Icon?>,
    public val username: Optional<String>,
    public val discriminator: Optional<String>,
    public val flags: Optional<UserFlags?>
) {
    /**
     * A Set representing the values that have changes. Each value is represented by a human-readable string.
     */
    public open val changes: Set<String> by lazy {
        mutableSetOf<String>().apply {
            if (avatar !is Optional.Missing) add("avatar")
            if (username !is Optional.Missing) add("username")
            if (discriminator !is Optional.Missing) add("discriminator")
            if (flags !is Optional.Missing) add("flags")
        }
    }

    public companion object {
        /**
         * Given an old and new [User] object, return a [UserDelta] representing the changes between them.
         *
         * @param old The older [User] object.
         * @param new The newer [User] object.
         */
        public fun from(old: User?, new: User): UserDelta? {
            contract {
                returns(null) implies (old == null)
            }

            old ?: return null

            return UserDelta(
                if (old.avatar?.url != new.avatar?.url) Optional(new.avatar) else Optional.Missing(),
                if (old.username != new.username) Optional(new.username) else Optional.Missing(),
                if (old.discriminator != new.discriminator) Optional(new.discriminator) else Optional.Missing(),
                if (old.publicFlags != new.publicFlags) Optional(new.publicFlags) else Optional.Missing()
            )
        }
    }
}
