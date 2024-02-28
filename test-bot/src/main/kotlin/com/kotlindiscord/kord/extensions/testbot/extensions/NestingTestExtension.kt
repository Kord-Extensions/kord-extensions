/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot.extensions

import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand

public class NestingTestExtension : Extension() {
	override val name: String = "test-nesting"

	override suspend fun setup() {
		publicSlashCommand {
			name = "root"
			description = "Insert thoughtful proverb here"

			group("group") {
				description = "Insert required description here"

				publicSubCommand {
					name = "subcommand"
					description = "...in a group!"

					action {
						respond {
							content = "Ah-ah-ah!"
						}
					}
				}
			}

			publicSubCommand {
				name = "subcommand"
				description = "...NEXT to a group!"

				action {
					respond {
						content = "Ah-ah-ah!"
					}
				}
			}
		}
	}
}
