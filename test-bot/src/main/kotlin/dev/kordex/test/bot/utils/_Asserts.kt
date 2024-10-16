/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.test.bot.utils

import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.withContext

public typealias AssertBody = (suspend () -> Any)?

public suspend fun CommandContext.assert(
	value: Boolean,
	failureMessage: AssertBody = null,
) {
	if (!value) {
		val message = failureMessage?.invoke()?.toString() ?: "Argument is not `true`."

		logError { "**Assertion failed:** $message" }

		throw DiscordRelayedException(
			CoreTranslations.Common.assertionFailed
				.withContext(this)
				.withNamedPlaceholders("message" to message)
		)
	}
}

public suspend fun CommandContext.assertFalse(
	value: Boolean,
	failureMessage: AssertBody = null,
) {
	if (value) {
		val message = failureMessage?.invoke()?.toString() ?: "Argument is not `false`."

		logError { "**Assertion failed:** $message" }

		throw DiscordRelayedException(
			CoreTranslations.Common.assertionFailed
				.withContext(this)
				.withNamedPlaceholders("message" to message)
		)
	}
}

public suspend fun CommandContext.assertEqual(
	left: Any?,
	right: Any?,
	failureMessage: AssertBody = null,
) {
	if (left != right) {
		val message = failureMessage?.invoke()?.toString() ?: "`$left` is not equal to `$right`"

		logError { "**Assertion failed:** $message" }

		throw DiscordRelayedException(
			CoreTranslations.Common.assertionFailed
				.withContext(this)
				.withNamedPlaceholders("message" to message)
		)
	}
}

public suspend fun CommandContext.assertNotEqual(
	left: Any?,
	right: Any?,
	failureMessage: AssertBody = null,
) {
	if (left == right) {
		val message = failureMessage?.invoke()?.toString() ?: "`$left` is equal to `$right`"

		logError { "**Assertion failed:** $message" }

		throw DiscordRelayedException(
			CoreTranslations.Common.assertionFailed
				.withContext(this)
				.withNamedPlaceholders("message" to message)
		)
	}
}
