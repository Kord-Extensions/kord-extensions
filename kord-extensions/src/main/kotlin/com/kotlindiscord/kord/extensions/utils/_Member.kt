package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.entity.Permission
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import kotlinx.coroutines.flow.toList

/**
 * Check if the user has the given [Role].
 *
 * @param role Role to check for
 * @return true if the user has the given role, false otherwise
 */
public suspend fun Member.hasRole(role: Role): Boolean = roles.toList().contains(role)

/**
 * Check if the user has all of the given roles.
 *
 * @param roles Roles to check for.
 * @return `true` if the user has all of the given roles, `false` otherwise.
 */
public suspend inline fun Member.hasRoles(vararg roles: Role): Boolean = hasRoles(roles.toList())

/**
 * Check if the user has all of the given roles.
 *
 * @param roles Roles to check for.
 * @return `true` if the user has all of the given roles, `false` otherwise.
 */
public suspend fun Member.hasRoles(roles: Collection<Role>): Boolean =
    if (roles.isEmpty()) {
        true
    } else {
        this.roles.toList().containsAll(roles)
    }

/**
 * Convenience function to retrieve a user's top [Role].
 *
 * @receiver The [Member] to get the top role for
 * @return The user's top role, or `null` if they have no roles
 */
public suspend fun Member.getTopRole(): Role? = roles.toList().maxOrNull()

/**
 * Convenience function to check whether a guild member has a permission.
 *
 * This function only checks for permissions based on roles, and does not deal with channel overrides. It will
 * always return `true` if the member has the `Administrator` permission in one of their roles.
 *
 * @receiver The [Member] check permissions for for
 * @return Whether the [Member] has the given permission, or the Administrator permission
 */
public suspend fun Member.hasPermission(perm: Permission): Boolean = perm in getPermissions()

/**
 * Convenience function to check whether a guild member has all of the given permissions.
 *
 * This function only checks for permissions based on roles, and does not deal with channel overrides. It will
 * always return `true` if the member has the `Administrator` permission.
 *
 * @receiver The [Member] check permissions for
 * @param perms The permissions to check for
 *
 * @return `true` if the collection is empty, or the [Member] has all of the given permissions, `false` otherwise
 */
public suspend inline fun Member.hasPermissions(vararg perms: Permission): Boolean = hasPermissions(perms.toList())

/**
 * Convenience function to check whether a guild member has all of the given permissions.
 *
 * This function only checks for permissions based on roles, and does not deal with channel overrides. It will
 * always return `true` if the member has the `Administrator` permission.
 *
 * @receiver The [Member] check permissions for
 * @param perms The permissions to check for
 *
 * @return `true` if the collection is empty, or the [Member] has all of the given permissions, `false` otherwise
 */
public suspend fun Member.hasPermissions(perms: Collection<Permission>): Boolean =
    if (perms.isEmpty()) {
        true
    } else {
        val permissions = getPermissions()

        perms.all { it in permissions }
    }
