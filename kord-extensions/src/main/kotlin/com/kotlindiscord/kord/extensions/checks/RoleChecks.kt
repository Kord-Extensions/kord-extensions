@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.kotlindiscord.kord.extensions.getTopRole
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

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
        val logger = KotlinLogging.logger("notHasRole")

        with(event) {
            val member = message.getAuthorAsMember() ?: return false
            val result = member.roles.toList().contains(role).not()

            logger.debug {
                if (result) {
                    "Check passed: User does not have ${role.name} role."
                } else {
                    "Check failed: User has ${role.name} role."
                }
            }

            return result
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

            return topRole == role
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

            return topRole != role
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

            return topRole > role
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user a [MessageCreateEvent] fired for is lower than a given role.
 *
 * Returns `true` if the user doesn't have any roles.
 *
 * @param role The role to compare to.
 */
fun topRoleLower(role: Role): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        val logger = KotlinLogging.logger("notHasRole")

        with(event) {
            val member = message.getAuthorAsMember() ?: return false
            val topRole = member.getTopRole() ?: return true

            return topRole < role
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

            return topRole >= role
        }
    }

    return ::inner
}

/**
 * Check asserting that the top role for the user a [MessageCreateEvent] fired for is lower than or equal to a given
 * role.
 *
 * Returns `true` if the user doesn't have any roles.
 *
 * @param role The role to compare to.
 */
fun topRoleLowerOrEqual(role: Role): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            val member = message.getAuthorAsMember() ?: return false
            val topRole = member.getTopRole() ?: return true

            return topRole <= role
        }
    }

    return ::inner
}
