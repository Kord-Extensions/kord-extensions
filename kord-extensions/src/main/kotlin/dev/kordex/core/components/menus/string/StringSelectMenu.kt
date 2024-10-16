/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.menus.string

import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.SelectOptionBuilder
import dev.kordex.core.components.menus.*
import dev.kordex.core.i18n.types.Key

/** Interface for string select menus. **/
public interface StringSelectMenu {
	/** List of options for the user to choose from. **/
	public val options: MutableList<SelectOptionBuilder>

	/** Add an option to this select menu. **/
	@Suppress("UnnecessaryParentheses")
	public suspend fun option(
		label: Key,
		value: String,

		body: (suspend StringSelectOption.() -> Unit) = {},
	) {
		val kordExBuilder = StringSelectOption(label, value)

		body(kordExBuilder)

		val builder = kordExBuilder.build()

		if ((builder.description?.length ?: 0) > DESCRIPTION_MAX) {
			error("Option descriptions must not be longer than $DESCRIPTION_MAX characters.")
		}

		if (builder.label.length > LABEL_MAX) {
			error("Option labels must not be longer than $LABEL_MAX characters.")
		}

		if (builder.value.length > VALUE_MAX) {
			error("Option values must not be longer than $VALUE_MAX characters.")
		}

		options.add(builder)
	}

	/** Apply the string select menu to an action row builder. **/
	public fun applyStringSelectMenu(selectMenu: SelectMenu<*, *>, builder: ActionRowBuilder) {
		if (selectMenu.maximumChoices == null || selectMenu.maximumChoices!! > options.size) {
			selectMenu.maximumChoices = options.size
		}

		builder.stringSelect(selectMenu.id) {
			this.allowedValues = selectMenu.minimumChoices..selectMenu.maximumChoices!!
			this.options.addAll(this@StringSelectMenu.options)
			this.placeholder = selectMenu.placeholder?.translate()
			this.disabled = selectMenu.disabled
		}
	}

	/** Validate the options of the string select menu. **/
	@Suppress("UnnecessaryParentheses")
	public fun validateOptions() {
		if (this.options.isEmpty()) {
			error("Menu components must have at least one option.")
		}

		if (this.options.size > OPTIONS_MAX) {
			error("Menu components must not have more than $OPTIONS_MAX options.")
		}
	}
}
