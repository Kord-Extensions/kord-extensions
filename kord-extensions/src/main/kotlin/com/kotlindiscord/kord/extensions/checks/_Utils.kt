/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.kord.core.event.Event

/**
 * Convenience function allowing you to pass this check context, if the checks provided in the body pass – even if the
 * check context is failing.
 *
 * This will only run the given checks if the current check context has failed. It also resets the check context's
 * passing state and failure message before running the given checks.
 *
 * ```kotlin
 * check {
 *     someCheck()
 *
 *     or {
 *         someOtherCheck()
 *     }
 * }
 * ```
 *
 * If both checks provide a message, this function will combine them.
 * Otherwise, it'll provide the message for whichever check provides one.
 *
 * **Note:** As always, placing multiple checks within an `or { }` block will result in a block where **all** of the
 * given checks must pass.
 */
public suspend fun <T : Event> CheckContext<T>.or(body: Check<T>) {
	val currentMessage = message

	if (!passed) {
		passed = true
		message = null

		body(this)
	}

	if (!passed && currentMessage != null) {
		message = if (message == null) {
			currentMessage
		} else {
			"$currentMessage | $message"
		}
	}
}

/** Silence the current check by removing any message it may have set. **/
public fun CheckContext<*>.silence() {
	message = null
}
