/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.utils.hasPermission
import com.kotlindiscord.kord.extensions.utils.hasPermissions
import com.kotlindiscord.kord.extensions.utils.permissionsForMember
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.Event
import mu.KotlinLogging

/**
 * Check asserting that the user an [Event] fired for has a given permission, or the Administrator permission.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param perm The permission to check for.
 */
public suspend fun CheckContext<*>.hasPermission(perm: Permission) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.hasPermission")
    val channel = channelFor(event) as? GuildChannel
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        fail()
    } else {
        val memberObj = member.asMember()

        val result = when {
            memberObj.hasPermission(Permission.Administrator) -> true
            channel != null -> channel.permissionsForMember(member.id).contains(perm)

            else -> memberObj.hasPermission(perm)
        }

        if (result) {
            logger.passed()

            pass()
        } else {
            logger.failed("Member $member does not have permission $perm")

            fail(
                translate(
                    "checks.hasPermission.failed",
                    replacements = arrayOf(perm.translate(locale))
                )
            )
        }
    }
}

/**
 * Check asserting that the user an [Event] fired for **does not have** a given permission **or** the Administrator
 * permission.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param perm The permission to check for.
 */
public suspend fun CheckContext<*>.notHasPermission(perm: Permission) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notHasPermission")
    val channel = channelFor(event) as? GuildChannel
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        pass()
    } else {
        val memberObj = member.asMember()

        val result = when {
            memberObj.hasPermission(Permission.Administrator) -> true
            channel != null -> channel.permissionsForMember(member.id).contains(perm)

            else -> memberObj.hasPermission(perm)
        }

        if (result) {
            logger.failed("Member $member has permission $perm")

            fail(
                translate(
                    "checks.notHasPermission.failed",
                    replacements = arrayOf(perm.translate(locale)),
                )
            )
        } else {
            logger.passed()

            pass()
        }
    }
}

/**
 * Check asserting that the user an [Event] fired for has a given permission set, or the Administrator permission.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param perms The permissions to check for.
 */
public suspend fun CheckContext<*>.hasPermissions(perms: Permissions) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.hasPermissions")
    val channel = channelFor(event) as? GuildChannel
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        fail()
    } else {
        val memberObj = member.asMember()

        val result = when {
            memberObj.hasPermission(Permission.Administrator) -> true
            channel != null -> channel.permissionsForMember(member.id).contains(perms)

            else -> memberObj.hasPermissions(perms.values)
        }

        if (result) {
            logger.passed()

            pass()
        } else {
            logger.failed("Member $member does not have permissions $perms")

            fail(
                translate(
                    "checks.hasPermissions.failed",
                    replacements = arrayOf(perms.values.forEach { it.translate(locale) })
                )
            )
        }
    }
}

/**
 * Check asserting that the user an [Event] fired for **does not have** a given permission set **or** the Administrator
 * permission.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param perms The permissions to check for.
 */
public suspend fun CheckContext<*>.notHasPermissions(perms: Permissions) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notHasPermissions")
    val channel = channelFor(event) as? GuildChannel
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        fail()
    } else {
        val memberObj = member.asMember()

        val result = when {
            memberObj.hasPermission(Permission.Administrator) -> true
            channel != null -> channel.permissionsForMember(member.id).contains(perms)

            else -> memberObj.hasPermissions(perms.values)
        }

        if (result) {
            logger.failed("Member $member has permissions $perms")

            fail(
                translate(
                    "checks.notHasPermissions.failed",
                    replacements = arrayOf(perms.values.forEach { it.translate(locale) })
                )
            )
        } else {
            logger.passed()

            pass()
        }
    }
}
