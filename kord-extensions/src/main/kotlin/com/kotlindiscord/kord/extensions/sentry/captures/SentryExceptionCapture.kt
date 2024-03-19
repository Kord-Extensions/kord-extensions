/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.sentry.captures

import io.sentry.Hint
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import org.jetbrains.annotations.ApiStatus

/**
 * Sentry capture type, used when submitting a caught [Throwable] to Sentry.
 *
 * @param throwable The [Throwable] to submit to Sentry.
 */
public class SentryExceptionCapture(
	public val throwable: Throwable,
) : SentryScopeCapture() {
	/** @suppress Function meant for internal use. **/
	@ApiStatus.Internal
	public fun captureThrowable(): SentryId {
		if (hints.isNotEmpty()) {
			val sentryHint = Hint()

			processMap(hints)
				.forEach(sentryHint::set)

			return Sentry.captureException(throwable, sentryHint)
		}

		return Sentry.captureException(throwable)
	}
}
