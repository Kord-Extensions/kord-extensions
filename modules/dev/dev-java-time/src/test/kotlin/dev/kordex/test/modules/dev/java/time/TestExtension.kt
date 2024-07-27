/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.test.modules.dev.java.time

import dev.kordex.core.commands.Arguments
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.chatCommand
import dev.kordex.core.utils.respond
import dev.kordex.modules.dev.java.time.coalescingJ8Duration
import dev.kordex.modules.dev.java.time.toHuman

// They're IDs
@Suppress("UnderscoresInNumericLiterals")
class TestExtension : Extension() {
	override val name = "test"

	class TestArgs : Arguments() {
		val duration by coalescingJ8Duration {
			name = "duration"
			description = "Duration argument"
		}
	}

	override suspend fun setup() {
		chatCommand(TestExtension::TestArgs) {
			name = "format"
			description = "Let's test formatting."

			action {
				message.respond(
					arguments.duration.toHuman(this) ?: "Empty duration!"
				)
			}
		}
	}
}
