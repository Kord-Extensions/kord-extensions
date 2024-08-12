/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
