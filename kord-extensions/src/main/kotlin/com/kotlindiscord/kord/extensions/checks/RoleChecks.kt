/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.utils.getTopRole
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.RoleBehavior
import dev.kord.core.event.Event
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

// region: Entity DSL versions

/**
 * Check asserting that the user an [Event] fired for has a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the role to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.hasRole(builder: suspend (T) -> RoleBehavior) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.hasRole")
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        fail()
    } else {
        val role = builder(event)

        if (member.asMember().roles.toList().contains(role)) {
            logger.passed()

            pass()
        } else {
            logger.failed("Member $member does not have role $role")

            fail(
                translate(
                    "checks.hasRole.failed",
                    replacements = arrayOf(role.mention),
                )
            )
        }
    }
}

/**
 * Check asserting that the user an [Event] fired for **does not have** a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the role to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.notHasRole(builder: suspend (T) -> RoleBehavior) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notHasRole")
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        pass()
    } else {
        val role = builder(event)

        if (member.asMember().roles.toList().contains(role)) {
            logger.failed("Member $member has role $role")

            fail(
                translate(
                    "checks.notHasRole.failed",
                    replacements = arrayOf(role.mention),
                )
            )
        } else {
            logger.passed()

            pass()
        }
    }
}

/**
 * Check asserting that the top role for the user an [Event] fired for is equal to a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the role to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.topRoleEqual(builder: suspend (T) -> RoleBehavior) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleEqual")
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        fail()
    } else {
        val role = builder(event)
        val topRole = member.asMember().getTopRole()

        when {
            topRole == null -> {
                logger.failed("Member $member has no top role")

                fail(
                    translate(
                        "checks.topRoleEqual.failed",
                        replacements = arrayOf(role.mention),
                    )
                )
            }

            topRole != role -> {
                logger.failed("Member $member does not have top role $role")

                fail(
                    translate(
                        "checks.topRoleEqual.failed",
                        replacements = arrayOf(role.mention),
                    )
                )
            }

            else -> {
                logger.passed()

                pass()
            }
        }
    }
}

/**
 * Check asserting that the top role for the user an [Event] fired for is **not** equal to a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the role to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.topRoleNotEqual(builder: suspend (T) -> RoleBehavior) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleNotEqual")
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        pass()
    } else {
        val role = builder(event)

        when (member.asMember().getTopRole()) {
            null -> {
                logger.passed("Member $member has no top role")

                pass()
            }

            role -> {
                logger.failed("Member $member has top role $role")

                fail(
                    translate(
                        "checks.topRoleNotEqual.failed",
                        replacements = arrayOf(role.mention),
                    )
                )
            }

            else -> {
                logger.passed()

                pass()
            }
        }
    }
}

/**
 * Check asserting that the top role for the user an [Event] fired for is higher than a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param builder Lambda returning the role to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.topRoleHigher(builder: suspend (T) -> RoleBehavior) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleHigher")
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        fail()
    } else {
        val role = builder(event)
        val topRole = member.asMember().getTopRole()

        when {
            topRole == null -> {
                logger.failed("Member $member has no top role")

                fail(
                    translate(
                        "checks.topRoleHigher.failed",
                        replacements = arrayOf(role.mention),
                    )
                )
            }

            topRole > role -> {
                logger.passed()

                pass()
            }

            else -> {
                logger.failed("Member $member has a top role less than or equal to $role")

                fail(
                    translate(
                        "checks.topRoleHigher.failed",
                        replacements = arrayOf(role.mention),
                    )
                )
            }
        }
    }
}

/**
 * Check asserting that the top role for the user an [Event] fired for is lower than a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * Returns `CheckResult.Passed()` if the user doesn't have any roles.
 *
 * @param builder Lambda returning the role to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.topRoleLower(builder: suspend (T) -> RoleBehavior) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleLower")
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        fail()
    } else {
        val role = builder(event)
        val topRole = member.asMember().getTopRole()

        when {
            topRole == null -> {
                logger.passed("Member $member has no top role")

                pass()
            }

            topRole < role -> {
                logger.passed()

                pass()
            }

            else -> {
                logger.failed("Member $member has a top role greater than or equal to $role")

                fail(
                    translate(
                        "checks.topRoleLower.failed",
                        replacements = arrayOf(role.mention),
                    )
                )
            }
        }
    }
}

/**
 * Check asserting that the top role for the user an [Event] fired for is higher than or equal to a given
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 * role.
 *
 * @param builder Lambda returning the role to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.topRoleHigherOrEqual(builder: suspend (T) -> RoleBehavior) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleHigherOrEqual")
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        fail()
    } else {
        val role = builder(event)
        val topRole = member.asMember().getTopRole()

        when {
            topRole == null -> {
                logger.failed("Member $member has no top role")

                fail(
                    translate(
                        "checks.topRoleHigherOrEqual.failed",
                        replacements = arrayOf(role.mention),
                    )
                )
            }

            topRole >= role -> {
                logger.passed()

                pass()
            }

            else -> {
                logger.failed("Member $member has a top role less than $role")

                fail(
                    translate(
                        "checks.topRoleHigherOrEqual.failed",
                        replacements = arrayOf(role.mention),
                    )
                )
            }
        }
    }
}

/**
 * Check asserting that the top role for the user an [Event] fired for is lower than or equal to a given
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 * role.
 *
 * Returns `CheckResult.Passed()` if the user doesn't have any roles.
 *
 * @param builder Lambda returning the role to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.topRoleLowerOrEqual(builder: suspend (T) -> RoleBehavior) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleLowerOrEqual")
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)

        fail()
    } else {
        val role = builder(event)
        val topRole = member.asMember().getTopRole()

        when {
            topRole == null -> {
                logger.passed("Member $member has no top role")

                pass()
            }

            topRole <= role -> {
                logger.passed()

                pass()
            }

            else -> {
                logger.failed("Member $member has a top role greater than $role")

                fail(
                    translate(
                        "checks.topRoleLowerOrEqual.failed",
                        replacements = arrayOf(role.mention),
                    )
                )
            }
        }
    }
}

// endregion

// region: Snowflake versions

/**
 * Check asserting that the user an [Event] fired for has a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Role snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.hasRole(id: Snowflake) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.hasRole")
    val role = guildFor(event)?.getRoleOrNull(id)

    if (role == null) {
        logger.noRoleId(id)

        fail()
    } else {
        hasRole { role }
    }
}

/**
 * Check asserting that the user an [Event] fired for **does not have** a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Role snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.notHasRole(id: Snowflake) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.notHasRole")
    val role = guildFor(event)?.getRoleOrNull(id)

    if (role == null) {
        logger.noRoleId(id)

        pass()
    } else {
        notHasRole { role }
    }
}

/**
 * Check asserting that the top role for the user an [Event] fired for is equal to a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Role snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.topRoleEqual(id: Snowflake) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleEqual")
    val role = guildFor(event)?.getRoleOrNull(id)

    if (role == null) {
        logger.noRoleId(id)

        fail()
    } else {
        topRoleEqual { role }
    }
}

/**
 * Check asserting that the top role for the user an [Event] fired for is **not** equal to a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Role snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.topRoleNotEqual(id: Snowflake) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleNotEqual")
    val role = guildFor(event)?.getRoleOrNull(id)

    if (role == null) {
        logger.noRoleId(id)

        pass()
    } else {
        topRoleNotEqual { role }
    }
}

/**
 * Check asserting that the top role for the user an [Event] fired for is higher than a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * @param id Role snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.topRoleHigher(id: Snowflake) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleHigher")
    val role = guildFor(event)?.getRoleOrNull(id)

    if (role == null) {
        logger.noRoleId(id)

        fail()
    } else {
        topRoleHigher { role }
    }
}

/**
 * Check asserting that the top role for the user an [Event] fired for is lower than a given role.
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 *
 * Returns `CheckResult.Passed()` if the user doesn't have any roles.
 *
 * @param id Role snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.topRoleLower(id: Snowflake) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleLower")
    val role = guildFor(event)?.getRoleOrNull(id)

    if (role == null) {
        logger.noRoleId(id)

        fail()
    } else {
        topRoleLower { role }
    }
}

/**
 * Check asserting that the top role for the user an [Event] fired for is higher than or equal to a given
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 * role.
 *
 * @param id Role snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.topRoleHigherOrEqual(id: Snowflake) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleHigherOrEqual")
    val role = guildFor(event)?.getRoleOrNull(id)

    if (role == null) {
        logger.noRoleId(id)

        fail()
    } else {
        topRoleHigherOrEqual { role }
    }
}

/**
 * Check asserting that the top role for the user an [Event] fired for is lower than or equal to a given
 *
 * Only events that can reasonably be associated with a guild member are supported. Please raise
 * an issue if an event you expected to be supported, isn't.
 * role.
 *
 * Returns `CheckResult.Passed()` if the user doesn't have any roles.
 *
 * @param id Role snowflake to compare to.
 */
public suspend fun <T : Event> CheckContext<T>.topRoleLowerOrEqual(id: Snowflake) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.topRoleLowerOrEqual")
    val role = guildFor(event)?.getRoleOrNull(id)

    if (role == null) {
        logger.noRoleId(id)

        fail()
    } else {
        topRoleLowerOrEqual { role }
    }
}

// endregion
