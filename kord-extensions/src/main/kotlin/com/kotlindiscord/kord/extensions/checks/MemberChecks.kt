@file:Suppress("RedundantSuspendModifier")

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.utils.getKoin
import com.kotlindiscord.kord.extensions.utils.hasPermission
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.common.entity.Permission
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.Event
import mu.KotlinLogging
import java.util.*

private val defaultLocale: Locale
    get() = getKoin().get<ExtensibleBotBuilder>().i18nBuilder.defaultLocale

/**
 * Check asserting that the user an [Event] fired for has a given permission, or the Administrator permission.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param perm The permission to check for.
 */
public fun hasPermission(perm: Permission): Check<*> = {
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
            channel != null -> channel.getEffectivePermissions(member.id).contains(perm)

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
public fun notHasPermission(perm: Permission): Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notHasPermission")
    val channel = channelFor(event) as? GuildChannel
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        fail()
    } else {
        val memberObj = member.asMember()

        val result = when {
            memberObj.hasPermission(Permission.Administrator) -> true
            channel != null -> channel.getEffectivePermissions(member.id).contains(perm)

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
