/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.forms.widgets

import dev.kord.common.entity.TextInputStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import java.util.*

public const val MAX_LENGTH: Int = 4000
public const val MIN_LENGTH: Int = 0
public const val LABEL_LENGTH: Int = 45
public const val PLACEHOLDER_LENGTH: Int = 100

public abstract class TextInputWidget<T : TextInputWidget<T>> : Widget<String?>() {
    override var width: Int = 5
    override var height: Int = 1
    override var value: String? = null

    public abstract val style: TextInputStyle
    public lateinit var label: String

    public abstract var callback: (T.(String) -> Unit)?

    public var id: String = UUID.randomUUID().toString()
    public var initialValue: String? = null
    public var maxLength: Int = MAX_LENGTH
    public var minLength: Int = MIN_LENGTH
    public var placeholder: String? = null
    public var required: Boolean = true

    public override fun validate() {
        if (this::label.isInitialized.not() || label.isEmpty()) {
            error("Text input widgets must be given a label, but no label was provided.")
        }

        if (label.length > 45) {
            error("Labels must be shorter than 45 characters, but ${label.length} characters were provided.")
        }

        if (maxLength !in (MIN_LENGTH + 1)..MAX_LENGTH) {
            error(
                "Invalid value for maxLength provided: $maxLength - expected ${MIN_LENGTH + 1} - $MAX_LENGTH"
            )
        }

        if (minLength !in MIN_LENGTH until MAX_LENGTH) {
            error(
                "Invalid value for minLength provided: $minLength - expected $MIN_LENGTH - ${MAX_LENGTH - 1}"
            )
        }

        if (initialValue != null && (initialValue!!.length > MAX_LENGTH || initialValue!!.isEmpty())) {
            error(
                "Invalid value for $initialValue provided: (${initialValue!!.length} chars) - expected " +
                    "${MIN_LENGTH + 1} - $MAX_LENGTH"
            )
        }

        if (placeholder != null && (placeholder!!.length > PLACEHOLDER_LENGTH || placeholder!!.isEmpty())) {
            error(
                "Invalid value for $placeholder provided: (${placeholder!!.length} chars) - expected " +
                    "${MIN_LENGTH + 1} - $PLACEHOLDER_LENGTH"
            )
        }
    }

    override suspend fun apply(builder: ActionRowBuilder) {
        builder.textInput(style, id, label) {
            this.allowedLength = this@TextInputWidget.minLength..this@TextInputWidget.maxLength
            this.placeholder = this@TextInputWidget.placeholder
            this.required = this@TextInputWidget.required
            this.value = this@TextInputWidget.initialValue
        }
    }
}
