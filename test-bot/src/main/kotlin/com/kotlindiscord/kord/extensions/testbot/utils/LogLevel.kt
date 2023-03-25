/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot.utils

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.DISCORD_YELLOW
import dev.kord.common.Color

public sealed class LogLevel(
    public val name: String,
    public val color: Color?,
    private val order: Int
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
