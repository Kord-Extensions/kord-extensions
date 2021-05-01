@file:Suppress("RedundantSuspendModifier", "StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.utils.getTopRole
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.RoleBehavior
import dev.kord.core.event.Event
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

// region: Entity DSL versions

/**
 * Check asserting that the user an [Event] fired for has a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the role to compare to.
 */
public fun hasRole(builder: suspend () -> RoleBehavior): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.hasRole")

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.nullMember(event)
            return false
        }

        val role = builder()

        return if (member.asMember().roles.toList().contains(role)) {
            logger.passed()
            true
        } else {
            logger.failed("Member $member does not have role $role")
            false
        }
    }

    return ::inner
}

/**
 * Check asserting that the user an [Event] fired for **does not have** a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the role to compare to.
 */
public fun notHasRole(builder: suspend () -> RoleBehavior): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notHasRole")

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.nullMember(event)
            return false
        }

        val role = builder()

        return if (member.asMember().roles.toList().contains(role)) {
            logger.failed("Member $member has role $role")
            false
        } else {
            logger.passed()
            true
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user an [Event] fired for is equal to a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the role to compare to.
 */
public fun topRoleEqual(builder: suspend () -> RoleBehavior): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleEqual")

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.nullMember(event)
            return false
        }

        val role = builder()
        val topRole = member.asMember().getTopRole()

        return when {
            topRole == null -> {
                logger.failed("Member $member has no top role")
                false
            }

            topRole != role -> {
                logger.failed("Member $member does not have top role $role")
                false
            }

            else -> {
                logger.passed()
                true
            }
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user an [Event] fired for is **not** equal to a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the role to compare to.
 */
public fun topRoleNotEqual(builder: suspend () -> RoleBehavior): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleNotEqual")

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.nullMember(event)
            return false
        }

        val role = builder()

        return when (member.asMember().getTopRole()) {
            null -> {
                logger.passed("Member $member has no top role")
                true
            }
            role -> {
                logger.failed("Member $member has top role $role")
                false
            }
            else -> {
                logger.passed()
                true
            }
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user an [Event] fired for is higher than a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the role to compare to.
 */
public fun topRoleHigher(builder: suspend () -> RoleBehavior): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleHigher")

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.nullMember(event)
            return false
        }

        val role = builder()
        val topRole = member.asMember().getTopRole()

        return when {
            topRole == null -> {
                logger.failed("Member $member has no top role")
                false
            }

            topRole > role -> {
                logger.passed()
                true
            }

            else -> {
                logger.failed("Member $member has a top role less than or equal to $role")
                false
            }
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user an [Event] fired for is lower than a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * Returns `true` if the user doesn't have any roles.
 *
 * @param builder Lambda returning the role to compare to.
 */
public fun topRoleLower(builder: suspend () -> RoleBehavior): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleLower")

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.nullMember(event)
            return false
        }

        val role = builder()
        val topRole = member.asMember().getTopRole()

        return when {
            topRole == null -> {
                logger.passed("Member $member has no top role")
                true
            }

            topRole < role -> {
                logger.passed()
                true
            }

            else -> {
                logger.failed("Member $member has a top role greater than or equal to $role")
                false
            }
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user an [Event] fired for is higher than or equal to a given
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 * role.
 *
 * @param builder Lambda returning the role to compare to.
 */
public fun topRoleHigherOrEqual(builder: suspend () -> RoleBehavior): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleHigherOrEqual")

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.nullMember(event)
            return false
        }

        val role = builder()
        val topRole = member.asMember().getTopRole()

        return when {
            topRole == null -> {
                logger.failed("Member $member has no top role")
                false
            }

            topRole >= role -> {
                logger.passed()
                true
            }

            else -> {
                logger.failed("Member $member has a top role less than $role")
                false
            }
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user an [Event] fired for is lower than or equal to a given
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 * role.
 *
 * Returns `true` if the user doesn't have any roles.
 *
 * @param builder Lambda returning the role to compare to.
 */
public fun topRoleLowerOrEqual(builder: suspend () -> RoleBehavior): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleLowerOrEqual")

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.nullMember(event)
            return false
        }

        val role = builder()
        val topRole = member.asMember().getTopRole()

        return when {
            topRole == null -> {
                logger.passed("Member $member has no top role")
                true
            }

            topRole <= role -> {
                logger.passed()
                true
            }

            else -> {
                logger.failed("Member $member has a top role greater than $role")
                false
            }
        }
    }

    return ::inner
}

// endregion

// region: Snowflake versions

/**
 * Check asserting that the user an [Event] fired for has a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Role snowflake to compare to.
 */
public fun hasRole(id: Snowflake): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.hasRole")

    suspend fun inner(event: Event): Boolean {
        val role = guildFor(event)?.getRoleOrNull(id)

        if (role == null) {
            logger.noRoleId(id)
            return false
        }

        return hasRole { role }(event)
    }

    return ::inner
}

/**
 * Check asserting that the user an [Event] fired for **does not have** a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Role snowflake to compare to.
 */
public fun notHasRole(id: Snowflake): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notHasRole")

    suspend fun inner(event: Event): Boolean {
        val role = guildFor(event)?.getRoleOrNull(id)

        if (role == null) {
            logger.noRoleId(id)
            return false
        }

        return notHasRole { role }(event)
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user an [Event] fired for is equal to a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Role snowflake to compare to.
 */
public fun topRoleEqual(id: Snowflake): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleEqual")

    suspend fun inner(event: Event): Boolean {
        val role = guildFor(event)?.getRoleOrNull(id)

        if (role == null) {
            logger.noRoleId(id)
            return false
        }

        return topRoleEqual { role }(event)
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user an [Event] fired for is **not** equal to a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Role snowflake to compare to.
 */
public fun topRoleNotEqual(id: Snowflake): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleNotEqual")

    suspend fun inner(event: Event): Boolean {
        val role = guildFor(event)?.getRoleOrNull(id)

        if (role == null) {
            logger.noRoleId(id)
            return false
        }

        return topRoleNotEqual { role }(event)
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user an [Event] fired for is higher than a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Role snowflake to compare to.
 */
public fun topRoleHigher(id: Snowflake): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleHigher")

    suspend fun inner(event: Event): Boolean {
        val role = guildFor(event)?.getRoleOrNull(id)

        if (role == null) {
            logger.noRoleId(id)
            return false
        }

        return topRoleHigher { role }(event)
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user an [Event] fired for is lower than a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * Returns `true` if the user doesn't have any roles.
 *
 * @param id Role snowflake to compare to.
 */
public fun topRoleLower(id: Snowflake): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleLower")

    suspend fun inner(event: Event): Boolean {
        val role = guildFor(event)?.getRoleOrNull(id)

        if (role == null) {
            logger.noRoleId(id)
            return false
        }

        return topRoleLower { role }(event)
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user an [Event] fired for is higher than or equal to a given
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 * role.
 *
 * @param id Role snowflake to compare to.
 */
public fun topRoleHigherOrEqual(id: Snowflake): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleHigherOrEqual")

    suspend fun inner(event: Event): Boolean {
        val role = guildFor(event)?.getRoleOrNull(id)

        if (role == null) {
            logger.noRoleId(id)
            return false
        }

        return topRoleHigherOrEqual { role }(event)
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user an [Event] fired for is lower than or equal to a given
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 * role.
 *
 * Returns `true` if the user doesn't have any roles.
 *
 * @param id Role snowflake to compare to.
 */
public fun topRoleLowerOrEqual(id: Snowflake): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleLowerOrEqual")

    suspend fun inner(event: Event): Boolean {
        val role = guildFor(event)?.getRoleOrNull(id)

        if (role == null) {
            logger.noRoleId(id)
            return false
        }

        return topRoleLowerOrEqual { role }(event)
    }

    return ::inner
}

// endregion
