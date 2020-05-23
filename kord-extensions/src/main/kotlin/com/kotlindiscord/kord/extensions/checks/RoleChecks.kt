@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.event.Event
import com.kotlindiscord.kord.extensions.InvalidEventHandlerException
import com.kotlindiscord.kord.extensions.getTopRole
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
fun hasRole(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        return member.asMember().roles.toList().contains(role)
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
fun notHasRole(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        return member.asMember().roles.toList().contains(role).not()
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
fun topRoleEqual(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        val topRole = member.asMember().getTopRole() ?: return false
        return topRole == role
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
fun topRoleNotEqual(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        val topRole = member.asMember().getTopRole() ?: return false
        return topRole != role
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
fun topRoleHigher(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        val topRole = member.asMember().getTopRole() ?: return false
        return topRole > role
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
fun topRoleLower(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        val topRole = member.asMember().getTopRole() ?: return false
        return topRole < role
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
fun topRoleHigherOrEqual(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        val topRole = member.asMember().getTopRole() ?: return false
        return topRole >= role
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
fun topRoleLowerOrEqual(role: Role): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        val topRole = member.asMember().getTopRole() ?: return false
        return topRole <= role
    }

    return ::inner
}
