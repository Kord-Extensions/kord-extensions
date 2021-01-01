package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import kotlinx.coroutines.flow.toList

/**
 * Check if the user has the provided [Role].
 *
 * @param role Role to check for
 *
 * @return true if the user has the given role, false otherwise
 */
public suspend fun Member.hasRole(role: Role): Boolean =
    this.roles.toList().contains(role)

/**
 * Convenience function to retrieve a user's top [Role].
 *
 * @receiver The [Member] to get the top role for
 * @return The user's top role, or `null` if they have no roles
 */
public suspend fun Member.getTopRole(): Role? = this.roles.toList().maxOrNull()

/**
 * Convenience function to check whether a guild member has a permission.
 *
 * This function only checks for permissions based on roles, and does not deal with channel overrides. It will
 * always return `true` if the member has the `Administrator` permission in one of their roles.
 *
 * @receiver The [Member] check permissions for for
 * @return Whether the [Member] has the given permission, or the Administrator permission
 */
public suspend fun Member.hasPermission(perm: Permission): Boolean {
    val permissions = roles.toList()
        .map { it.permissions }
        .reduce { left, right -> Permissions(left.code + right.code) }

    return perm in permissions || Permission.Administrator in permissions
}
