/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.modules.func.mappings

import dev.kordex.core.ExtensibleBot
import dev.kordex.core.checks.isNotBot
import dev.kordex.core.utils.env
import dev.kordex.modules.func.mappings.extMappings
import org.koin.core.logger.Level

suspend fun main() {
	val bot = ExtensibleBot(env("TOKEN")) {
		koinLogLevel = Level.DEBUG

		chatCommands {
			check { isNotBot() }
			enabled = true
		}

		applicationCommands {
			enabled = true
		}

		extensions {
			extMappings {
//                namespaceCheck { namespace ->
//                    {
//                        if (namespace == YarnNamespace) {
//                            pass()
//                        } else {
//                            fail("Yarn only, ya dummy.")
//                        }
//                    }
//                }
			}
		}
	}

	bot.start()
}
