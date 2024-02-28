/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.slash.converters

import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter

private const val CHOICE_LIMIT = 25  // Discord doesn't allow more choices than this

/**
 * Special [SingleConverter] designed for slash commands, allowing you to specify up to 10 choices for the user.
 *
 * @property choices List of choices for the user to pick from.
 */

public abstract class ChoiceConverter<T : Any>(
	public open val choices: Map<String, T>,
) : SingleConverter<T>() {
	init {
		if (choices.size > CHOICE_LIMIT) {
			error("You may not provide more than $CHOICE_LIMIT choices.")
		}
	}
}
