@file:JvmMultifileClass
@file:JvmName("MemberKt")

package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.entity.Permission
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import kotlinx.coroutines.flow.toList

/**
 * Check if the user has the roles
 * @param roles Roles to check for
 * @return `true` if the user has all roles, `false` otherwise
 */
public suspend inline fun Member.hasRoles(vararg roles: Role): Boolean
    = this.hasRoles(roles.toList())

/**
 * Check if the user has the roles
 * @param roles Roles to check for
 * @return `true` if the user has all roles, `false` otherwise
 */
public suspend fun Member.hasRoles(roles: Collection<Role>): Boolean = when {
    roles.isEmpty() -> true
    else -> this.roles.toList().containsAll(roles)
}

/**
 * Convenience function to retrieve a user's top [Role].
 *
 * @receiver The [Member] to get the top role for
 * @return The user's top role, or `null` if they have no roles
 */
public suspend fun Member.getTopRole(): Role?
    = roles.toList().maxOrNull()

/**
 * Convenience function to check whether a guild member has the permissions.
 *
 * This function only checks for permissions based on roles, and does not deal with channel overrides. It will
 * always return `true` if the member has the `Administrator` permission in one of their roles.
 *
 * @receiver The [Member] check permissions for
 * @param perms The permissions to check for
 * @return `true` if the collection is empty,
 * or the [Member] has the permissions (also if he has the administrator permission),
 * `false` otherwise
 */
public suspend inline fun Member.hasPermissions(vararg perms: Permission): Boolean
    = hasPermissions(perms.toList())

/**
 * Convenience function to check whether a guild member has the permissions.
 *
 * This function only checks for permissions based on roles, and does not deal with channel overrides. It will
 * always return `true` if the member has the `Administrator` permission in one of their roles.
 *
 * @receiver The [Member] check permissions for
 * @param perms The permissions to check for
 * @return `true` if the collection is empty, 
 * or the [Member] has the permissions (also if he has the administrator permission),
 * `false` otherwise
 */
public suspend fun Member.hasPermissions(perms: Collection<Permission>): Boolean {
    if(perms.isEmpty()) return true
    val permissions = getPermissions()
    return perms.all { it in permissions }
}
