/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.checks

import dev.kord.core.event.Event
import dev.kordex.core.checks.types.Check
import dev.kordex.core.checks.types.CheckContext

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
