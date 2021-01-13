@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.Permission
import dev.kord.core.event.Event
import mu.KotlinLogging

/**
 * Check asserting that the user an [Event] fired for has a given permission.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * Additionally, this check only operates on role permissions, and ignores channel overrides.
 *
 * @param perm The permission to check for.
 */
public fun hasPermission(perm: Permission): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.hasPermission")

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        return if (member.asMember().hasPermission(perm)) {
            logger.debug { "Passing check" }
            true
        } else {
            logger.debug { "Failing check: Member $member does not have permission $perm" }
            false
        }
    }

    return ::inner
}

/**
 * Check asserting that the user an [Event] fired for **does not have** a given permission.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * Additionally, this check only operates on role permissions, and ignores channel overrides.
 *
 * @param perm The permission to check for.
 */
public fun notHasPermission(perm: Permission): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notHasPermission")

    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event)

        if (member == null) {
            logger.debug { "Member for event $event is null. This type of event may not be supported." }
            return false
        }

        return if (member.asMember().hasPermission(perm)) {
            logger.debug { "Failing check: Member $member has permission $perm" }
            false
        } else {
            logger.debug { "Passing check" }
            true
        }
    }

    return ::inner
}
