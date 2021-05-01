@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.Permission
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
public fun hasPermission(perm: Permission): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.hasPermission")

    suspend fun inner(event: Event): Boolean {
        val channel = channelFor(event) as? GuildChannel
        val member = memberFor(event)

        if (member == null) {
            logger.nullMember(event)
            return false
        }

        val memberObj = member.asMember()

        val result = when {
            memberObj.hasPermission(Permission.Administrator) -> true
            channel != null -> channel.getEffectivePermissions(member.id).contains(perm)

            else -> memberObj.hasPermission(perm)
        }

        return if (result) {
            logger.passed()
            true
        } else {
            logger.failed("Member $member does not have permission $perm")
            false
        }
    }

    return ::inner
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
public fun notHasPermission(perm: Permission): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notHasPermission")

    suspend fun inner(event: Event): Boolean {
        val channel = channelFor(event) as? GuildChannel
        val member = memberFor(event)

        if (member == null) {
            logger.nullMember(event)
            return false
        }

        val memberObj = member.asMember()

        val result = when {
            memberObj.hasPermission(Permission.Administrator) -> true
            channel != null -> channel.getEffectivePermissions(member.id).contains(perm)

            else -> memberObj.hasPermission(perm)
        }

        return if (result) {
            logger.failed("Member $member has permission $perm")
            false
        } else {
            logger.passed()
            true
        }
    }

    return ::inner
}
