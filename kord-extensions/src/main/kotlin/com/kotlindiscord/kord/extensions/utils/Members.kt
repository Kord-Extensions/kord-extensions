package com.kotlindiscord.kord.extensions.utils

import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.Role
import kotlinx.coroutines.flow.toList

/**
 * Check if the user has the provided [Role].
 *
 * @param role Role to check for
 *
 * @return true if the user has the given role, false otherwise
 */
suspend fun Member.hasRole(role: Role): Boolean =
    this.roles.toList().contains(role)

/**
 * Convenience function to retrieve a user's top [Role].
 *
 * @receiver The [Member] to get the top role for
 * @return The user's top role, or `null` if they have no roles
 */
suspend fun Member.getTopRole(): Role? = this.roles.toList().maxOrNull()
