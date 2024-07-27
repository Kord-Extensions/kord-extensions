/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.test.modules.dev.time4j

import dev.kordex.core.commands.Arguments
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.chatCommand
import dev.kordex.core.utils.respond
import dev.kordex.modules.dev.time4j.coalescingT4JDuration
import dev.kordex.modules.dev.time4j.toHuman

// They're IDs
@Suppress("UnderscoresInNumericLiterals")
class TestExtension : Extension() {
	override val name = "test"

	class TestArgs : Arguments() {
		val duration by coalescingT4JDuration {
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
