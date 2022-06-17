/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.testbot.utils

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.CommandContext

public typealias AssertBody = (suspend () -> Any)?

public suspend fun CommandContext.assert(
    value: Boolean,
    failureMessage: AssertBody = null
) {
    if (!value) {
        val message = failureMessage?.invoke()?.toString() ?: "Argument is not `true`."

        logError { "**Assertion failed:** $message" }

        throw DiscordRelayedException("**Assertion failed:** $message")
    }
}

public suspend fun CommandContext.assertFalse(
    value: Boolean,
    failureMessage: AssertBody = null
) {
    if (value) {
        val message = failureMessage?.invoke()?.toString() ?: "Argument is not `false`."

        logError { "**Assertion failed:** $message" }

        throw DiscordRelayedException("**Assertion failed:** $message")
    }
}

public suspend fun CommandContext.assertEqual(
    left: Any?,
    right: Any?,
    failureMessage: AssertBody = null
) {
    if (left != right) {
        val message = failureMessage?.invoke()?.toString() ?: "`$left` is not equal to `$right`"

        logError { "**Assertion failed:** $message" }

        throw DiscordRelayedException("**Assertion failed:** $message")
    }
}

public suspend fun CommandContext.assertNotEqual(
    left: Any?,
    right: Any?,
    failureMessage: AssertBody = null
) {
    if (left == right) {
        val message = failureMessage?.invoke()?.toString() ?: "`$left` is equal to `$right`"

        logError { "**Assertion failed:** $message" }

        throw DiscordRelayedException("**Assertion failed:** $message")
    }
}
