/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.test.modules.dev.time4j

import dev.kord.common.entity.Snowflake
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.utils.env
import org.koin.core.logger.Level

val TEST_SERVER_ID = Snowflake(787452339908116521UL)

suspend fun main() {
	val bot = ExtensibleBot(env("TOKEN")) {
		koinLogLevel = Level.DEBUG

		chatCommands {
			defaultPrefix = "?"

			prefix { default ->
				if (guildId == TEST_SERVER_ID) {
					"!"
				} else {
					default  // "?"
				}
			}
		}

		extensions {
			add(::TestExtension)
		}
	}

	bot.start()
}
