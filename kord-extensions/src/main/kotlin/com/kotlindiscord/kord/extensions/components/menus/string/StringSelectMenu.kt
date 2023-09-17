/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus.string

import com.kotlindiscord.kord.extensions.components.menus.*
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.SelectOptionBuilder
import dev.kord.rest.builder.component.StringSelectBuilder
import dev.kord.rest.builder.component.options

/** Maximum length for an option's description. **/
public const val DESCRIPTION_MAX: Int = 100

/** Maximum length for an option's label. **/
public const val LABEL_MAX: Int = 100

/** Maximum length for an option's value. **/
public const val VALUE_MAX: Int = 100

/** Interface for string select menus. **/
public interface StringSelectMenu {
    /** List of options for the user to choose from. **/
    public val options: MutableList<SelectOptionBuilder>

    /** Add an option to this select menu. **/
    @Suppress("UnnecessaryParentheses")
    public suspend fun option(
        label: String,
        value: String,

        // TODO: Check this is fixed in later versions of the compiler
        // This is nullable like this due to a compiler bug: https://youtrack.jetbrains.com/issue/KT-51820
        body: (suspend SelectOptionBuilder.() -> Unit)? = null,
    ) {
        val builder = SelectOptionBuilder(label, value)

        if (body != null) {
            body(builder)
        }

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
            ((this as? StringSelectBuilder)?.options ?: mutableListOf()).addAll(this@StringSelectMenu.options)
            this.placeholder = selectMenu.placeholder
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
