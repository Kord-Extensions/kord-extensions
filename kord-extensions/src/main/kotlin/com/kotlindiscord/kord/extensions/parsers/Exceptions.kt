/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.parsers

import com.kotlindiscord.kord.extensions.KordExException

/**
 * A base exception class for parsers.
 */
public open class BaseParserException : KordExException()

/**
 * Generic duration parser exception.
 *
 * @param error Human-readable error text.
 */
public open class DurationParserException(public open var error: String) : BaseParserException() {
	override val message: String? = error
	override fun toString(): String = error
}

/**
 * Thrown when invalid time unit given to duration parser.
 *
 * @param unit Invalid unit.
 */
public class InvalidTimeUnitException(public val unit: String) : DurationParserException(unit)
