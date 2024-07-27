/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
