/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.application.slash.converters

import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.i18n.types.Key

private const val CHOICE_LIMIT = 25  // Discord doesn't allow more choices than this

/**
 * Special [SingleConverter] designed for slash commands, allowing you to specify up to 10 choices for the user.
 *
 * @property choices List of choices for the user to pick from.
 */

public abstract class ChoiceConverter<T : Any>(
	public open val choices: Map<Key, T>,
) : SingleConverter<T>() {
	init {
		if (choices.size > CHOICE_LIMIT) {
			error("You may not provide more than $CHOICE_LIMIT choices.")
		}
	}
}
