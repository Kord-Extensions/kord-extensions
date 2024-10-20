/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(NotTranslated::class)

package dev.kordex.core.checks

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.Event
import dev.kordex.core.annotations.NotTranslated
import dev.kordex.core.checks.types.CheckContext
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.utils.hasPermission
import dev.kordex.core.utils.hasPermissions
import dev.kordex.core.utils.permissionsForMember
import dev.kordex.core.utils.toTranslationKey
import dev.kordex.core.utils.translate
import io.github.oshai.kotlinlogging.KotlinLogging

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

	val logger = KotlinLogging.logger("dev.kordex.core.checks.hasPermission")
	val channel = channelFor(event) as? GuildChannel
	val member = memberFor(event)

	if (member == null) {
		logger.nullMember(event)

		fail()
	} else {
		val memberObj = member.asMember()

		val result = when {
			// TODO: Remove this when Kord fixes their function
			memberObj.permissions?.contains(Permission.Administrator) == true -> true
			memberObj.permissions?.contains(perm) == true -> true

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
				CoreTranslations.Checks.HasPermission.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(perm.toTranslationKey())
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

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notHasPermission")
	val channel = channelFor(event) as? GuildChannel
	val member = memberFor(event)

	if (member == null) {
		logger.nullMember(event)

		pass()
	} else {
		val memberObj = member.asMember()

		val result = when {
			// TODO: Remove this when Kord fixes their function
			memberObj.permissions?.contains(Permission.Administrator) == true -> true
			memberObj.permissions?.contains(perm) == true -> true

			memberObj.hasPermission(Permission.Administrator) -> true

			channel != null -> channel.permissionsForMember(member.id).contains(perm)

			else -> memberObj.hasPermission(perm)
		}

		if (result) {
			logger.failed("Member $member has permission $perm")

			fail(
				CoreTranslations.Checks.NotHasPermission.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(perm.toTranslationKey())
			)
		} else {
			logger.passed()

			pass()
		}
	}
}

/**
 * Check asserting that the user an [Event] fired for has the given permissions set, or the Administrator permission.
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

	val logger = KotlinLogging.logger("dev.kordex.core.checks.hasPermissions")
	val channel = channelFor(event) as? GuildChannel
	val member = memberFor(event)

	if (member == null) {
		logger.nullMember(event)

		fail()
	} else {
		val memberObj = member.asMember()

		val result = when {
			// TODO: Remove this when Kord fixes their function
			memberObj.permissions?.contains(Permission.Administrator) == true -> true
			memberObj.permissions?.contains(perms) == true -> true

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
				CoreTranslations.Checks.HasPermissions.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(
						perms.values.joinToString(", ") { it.translate(locale) }
					)
			)
		}
	}
}

/**
 * Check asserting that the user an [Event] fired for **does not have** the given permissions set **or** the
 * Administrator permission.
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

	val logger = KotlinLogging.logger("dev.kordex.core.checks.notHasPermissions")
	val channel = channelFor(event) as? GuildChannel
	val member = memberFor(event)

	if (member == null) {
		logger.nullMember(event)

		pass()
	} else {
		val memberObj = member.asMember()

		val result = when {
			// TODO: Remove this when Kord fixes their function
			memberObj.permissions?.contains(Permission.Administrator) == true -> true
			memberObj.permissions?.contains(perms) == true -> true

			memberObj.hasPermission(Permission.Administrator) -> true

			channel != null -> channel.permissionsForMember(member.id).contains(perms)

			else -> memberObj.hasPermissions(perms.values)
		}

		if (result) {
			logger.failed("Member $member has permissions $perms")

			fail(
				CoreTranslations.Checks.NotHasPermissions.failed
					.withLocale(locale)
					.withOrdinalPlaceholders(
						perms.values.joinToString(", ") { it.translate(locale) }
					)
			)
		} else {
			logger.passed()

			pass()
		}
	}
}
