/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.sentry.captures

import dev.kordex.core.utils.runSuspended
import io.sentry.Hint
import io.sentry.IScope
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
	public suspend fun captureThrowable(
		callback: (IScope) -> Unit = {},
	): SentryId {
		if (hints.isNotEmpty()) {
			val sentryHint = Hint()

			processMap(hints)
				.forEach(sentryHint::set)

			return runSuspended {
				Sentry.captureException(throwable, sentryHint, callback)
			}
		}

		return runSuspended {
			Sentry.captureException(throwable) {
				callback(it)
			}
		}
	}
}
