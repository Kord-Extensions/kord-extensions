@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.kotlindiscord.kord.extensions.getTopRole
import kotlinx.coroutines.flow.toList

/**
 * Check asserting that the user a [MessageCreateEvent] fired for has a given role.
 *
 * @param role The role to compare to.
 */
fun hasRole(role: Role): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            val member = message.getAuthorAsMember() ?: return false

            return member.roles.toList().contains(role)
        }
    }

    return ::inner
}

/**
 * Check asserting that the user a [MessageCreateEvent] fired for **does not have** a given role.
 *
 * @param role The role to compare to.
 */
fun notHasRole(role: Role): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            val member = message.getAuthorAsMember() ?: return false

            return member.roles.toList().contains(role).not()
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user a [MessageCreateEvent] fired for is equal to a given role.
 *
 * @param role The role to compare to.
 */
fun topRoleEqual(role: Role): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            val member = message.getAuthorAsMember() ?: return false
            val topRole = member.getTopRole() ?: return false

            return role == topRole
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user a [MessageCreateEvent] fired for is **not** equal to a given role.
 *
 * @param role The role to compare to.
 */
fun topRoleNotEqual(role: Role): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            val member = message.getAuthorAsMember() ?: return false
            val topRole = member.getTopRole() ?: return false

            return role != topRole
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user a [MessageCreateEvent] fired for is higher than a given role.
 *
 * @param role The role to compare to.
 */
fun topRoleHigher(role: Role): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            val member = message.getAuthorAsMember() ?: return false
            val topRole = member.getTopRole() ?: return false

            return role > topRole
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user a [MessageCreateEvent] fired for is lower than a given role.
 *
 * @param role The role to compare to.
 */
fun topRoleLower(role: Role): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            val member = message.getAuthorAsMember() ?: return false
            val topRole = member.getTopRole() ?: return false

            return role < topRole
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user a [MessageCreateEvent] fired for is higher than or equal to a given
 * role.
 *
 * @param role The role to compare to.
 */
fun topRoleHigherOrEqual(role: Role): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            val member = message.getAuthorAsMember() ?: return false
            val topRole = member.getTopRole() ?: return false

            return role >= topRole
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user a [MessageCreateEvent] fired for is lower than or equal to a given
 * role.
 *
 * @param role The role to compare to.
 */
fun topRoleLowerOrEqual(role: Role): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            val member = message.getAuthorAsMember() ?: return false
            val topRole = member.getTopRole() ?: return false

            return role <= topRole
        }
    }

    return ::inner
}
