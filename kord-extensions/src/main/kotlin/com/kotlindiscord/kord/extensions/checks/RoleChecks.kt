@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import dev.kord.core.entity.Role
import dev.kord.core.event.Event
import com.kotlindiscord.kord.extensions.utils.getTopRole
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

/**
 * Check asserting that the user an [Event] fired for has a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param role The role to compare to.
 */
public fun hasRole(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        return if (member.asMember().roles.toList().contains(role)) {
            logger.debug { "Passing check" }
            true
        } else {
            logger.debug { "Failing check: Member $member does not have role $role" }
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
 * @param role The role to compare to.
 */
public fun notHasRole(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        return if (member.asMember().roles.toList().contains(role)) {
            logger.debug { "Failing check: Member $member has role $role" }
            false
        } else {
            logger.debug { "Passing check" }
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
 * @param role The role to compare to.
 */
public fun topRoleEqual(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        val topRole = member.asMember().getTopRole()

        return when {
            topRole == null -> {
                logger.debug { "Failing check: Member $member has no top role" }
                false
            }

            topRole != role -> {
                logger.debug { "Failing check: Member $member does not have top role $role" }
                false
            }

            else -> {
                logger.debug { "Passing check" }
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
 * @param role The role to compare to.
 */
public fun topRoleNotEqual(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        return when (member.asMember().getTopRole()) {
            null -> {
                logger.debug { "Passing check: Member $member has no top role" }
                true
            }
            role -> {
                logger.debug { "Failing check: Member $member has top role $role" }
                false
            }
            else -> {
                logger.debug { "Passing check" }
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
 * @param role The role to compare to.
 */
public fun topRoleHigher(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        val topRole = member.asMember().getTopRole()

        return when {
            topRole == null -> {
                logger.debug { "Failing check: Member $member has no top role" }
                false
            }

            topRole > role -> {
                logger.debug { "Passing check" }
                true
            }

            else -> {
                logger.debug { "Failing check: Member $member has a top role less than or equal to $role" }
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
 * @param role The role to compare to.
 */
public fun topRoleLower(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        val topRole = member.asMember().getTopRole()

        return when {
            topRole == null -> {
                logger.debug { "Passing check: Member $member has no top role" }
                true
            }

            topRole < role -> {
                logger.debug { "Passing check" }
                true
            }

            else -> {
                logger.debug { "Failing check: Member $member has a top role greater than or equal to $role" }
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
 * @param role The role to compare to.
 */
public fun topRoleHigherOrEqual(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        val topRole = member.asMember().getTopRole()

        return when {
            topRole == null -> {
                logger.debug { "Failing check: Member $member has no top role" }
                false
            }

            topRole >= role -> {
                logger.debug { "Passing check" }
                true
            }

            else -> {
                logger.debug { "Failing check: Member $member has a top role less than $role" }
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
 * @param role The role to compare to.
 */
public fun topRoleLowerOrEqual(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        val topRole = member.asMember().getTopRole()

        return when {
            topRole == null -> {
                logger.debug { "Passing check: Member $member has no top role" }
                true
            }

            topRole <= role -> {
                logger.debug { "Passing check" }
                true
            }

            else -> {
                logger.debug { "Failing check: Member $member has a top role greater than $role" }
                false
            }
        }
    }

    return ::inner
}
