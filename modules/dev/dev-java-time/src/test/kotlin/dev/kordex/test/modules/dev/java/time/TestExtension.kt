/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.modules.dev.java.time

import dev.kordex.core.commands.Arguments
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.chatCommand
import dev.kordex.core.i18n.toKey
import dev.kordex.core.utils.respond
import dev.kordex.modules.dev.java.time.coalescingJ8Duration
import dev.kordex.modules.dev.java.time.toHuman

// They're IDs
@Suppress("UnderscoresInNumericLiterals")
class TestExtension : Extension() {
	override val name = "test"

	class TestArgs : Arguments() {
		val duration by coalescingJ8Duration {
			name = "duration".toKey()
			description = "Duration argument".toKey()
		}
	}

	override suspend fun setup() {
		chatCommand(TestExtension::TestArgs) {
			name = "format".toKey()
			description = "Let's test formatting.".toKey()

			action {
				message.respond(
					arguments.duration.toHuman(this) ?: "Empty duration!"
				)
			}
		}
	}
}
