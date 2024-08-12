/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("NOTHING_TO_INLINE")

package dev.kordex.core.checks

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.Event
import io.github.oshai.kotlinlogging.KLogger

/** Convenience wrapper for a "failing check" log message. **/
public inline fun KLogger.failed(reason: String): Unit =
	debug { "Failing check: $reason" }

/** Convenience wrapper for a "passing check" log message. **/
public inline fun KLogger.passed(): Unit =
	debug { "Passing check." }

/** Convenience wrapper for a "passing check" log message. **/
public inline fun KLogger.passed(reason: String): Unit =
	debug { "Passing check: $reason" }

/** Convenience wrapper for a "channel for event is null" log message. **/
public inline fun KLogger.nullChannel(event: Event): Unit =
	debug { "Channel for event $event is null. This type of event may not be supported." }

/** Convenience wrapper for a "guild for event is null" log message. **/
public inline fun KLogger.nullGuild(event: Event): Unit =
	debug { "Guild for event $event is null. This type of event may not be supported." }

/** Convenience wrapper for a "member for event is null" log message. **/
public inline fun KLogger.nullMember(event: Event): Unit =
	debug { "Member for event $event is null. This type of event may not be supported." }

/** Convenience wrapper for a "message for event is null" log message. **/
public inline fun KLogger.nullMessage(event: Event): Unit =
	debug { "Message for event $event is null. This type of event may not be supported." }

/** Convenience wrapper for a "failing: no such channel" log message. **/
public inline fun KLogger.noChannelId(id: Snowflake): Unit =
	failed("No such channel: ${id.value}")

/** Convenience wrapper for a "failing: no such category" log message. **/
public inline fun KLogger.noCategoryId(id: Snowflake): Unit =
	failed("No such category: ${id.value}")

/** Convenience wrapper for a "failing: no such guild" log message. **/
public inline fun KLogger.noGuildId(id: Snowflake): Unit =
	failed("No such guild: ${id.value}")

/** Convenience wrapper for a "failing: no such role" log message. **/
public inline fun KLogger.noRoleId(id: Snowflake): Unit =
	failed("No such role: ${id.value}")
