/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.modules.extra.mappings.extMappings
import com.kotlindiscord.kord.extensions.utils.env
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
