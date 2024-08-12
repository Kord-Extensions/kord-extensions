/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.bot.utils

import dev.kord.common.Color
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.DISCORD_RED
import dev.kordex.core.DISCORD_YELLOW

public sealed class LogLevel(
	public val name: String,
	public val color: Color?,
	private val order: Int,
) : Comparable<LogLevel> {
	public object ERROR : LogLevel("ERROR", DISCORD_RED, 4)
	public object WARNING : LogLevel("WARNING", DISCORD_YELLOW, 3)
	public object INFO : LogLevel("INFO", DISCORD_BLURPLE, 2)
	public object DEBUG : LogLevel("DEBUG", null, 1)

	public fun isEnabled(): Boolean =
		this in enabled

	override fun compareTo(other: LogLevel): Int =
		order.compareTo(other.order)

	public companion object {
		@Suppress("MemberVisibilityCanBePrivate")
		public val ALL: Set<LogLevel> = setOf(ERROR, WARNING, INFO, DEBUG)
		public val WARN: WARNING = WARNING

		public var enabledLevel: LogLevel = INFO
			set(value) {
				field = value

				enabled = ALL.filter {
					it.order >= value.order
				}.toSet()
			}

		public var enabled: Set<LogLevel> = ALL.filter {
			it.order >= enabledLevel.order
		}.toSet()

		public fun fromString(string: String): LogLevel? = when (string.uppercase()) {
			ERROR.toString() -> ERROR
			WARNING.toString() -> WARNING
			INFO.toString() -> INFO
			DEBUG.toString() -> DEBUG

			"WARN" -> WARNING

			else -> null
		}
	}
}
