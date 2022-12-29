/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus.string

import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.components.menus.*
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.SelectOptionBuilder

/** Abstract class representing a string select (dropdown) menu component. **/
public abstract class StringSelectMenu<C : StringSelectMenuContext, M : ModalForm>(timeoutTask: Task?) :
    SelectMenu<C, M>(timeoutTask) {

    /** List of options for the user to choose from. **/
    public val options: MutableList<SelectOptionBuilder> = mutableListOf()

    /** Add an option to this select menu. **/
    @Suppress("UnnecessaryParentheses")
    public open suspend fun option(
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

    public override fun apply(builder: ActionRowBuilder) {
        if (maximumChoices == null || maximumChoices!! > options.size) {
            maximumChoices = options.size
        }

        builder.stringSelect(id) {
            this.allowedValues = minimumChoices..maximumChoices!!

            @Suppress("DEPRECATION")  // Kord suppresses this in their own class
            this.options.addAll(this@StringSelectMenu.options)
            this.placeholder = this@StringSelectMenu.placeholder

            this.disabled = this@StringSelectMenu.disabled
        }
    }

    @Suppress("UnnecessaryParentheses")
    override fun validate() {
        super.validate()

        if (this.options.isEmpty()) {
            error("Menu components must have at least one option.")
        }

        if (this.options.size > OPTIONS_MAX) {
            error("Menu components must not have more than $OPTIONS_MAX options.")
        }

        if ((this.placeholder?.length ?: 0) > PLACEHOLDER_MAX) {
            error("Menu components must not have a placeholder longer than $PLACEHOLDER_MAX characters.")
        }
    }
}
