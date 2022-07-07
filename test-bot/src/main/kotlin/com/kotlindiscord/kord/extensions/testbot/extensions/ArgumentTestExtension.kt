/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.suggestStringMap

public class ArgumentTestExtension : Extension() {
    override val name: String = "test-args"

    override suspend fun setup() {
        publicSlashCommand(::OptionalArgs) {
            name = "optional-autocomplete"
            description = "Check whether autocomplete works with an optional converter."

            action {
                respond {
                    content = "You provided: `${arguments.response}`"
                }
            }
        }
    }

    public inner class OptionalArgs : Arguments() {
        public val response: String? by optionalString {
            name = "response"
            description = "Text to receive"

            autoComplete {
                suggestStringMap(
                    mapOf(
                        "one" to "One",
                        "two" to "Two",
                        "three" to "Three"
                    )
                )
            }
        }
    }
}
