/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.forms.widgets

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.TextInputStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.inject
import java.util.*

/** The max number of characters that can be present in the widget's input. **/
public const val MAX_LENGTH: Int = 4000

/** The min number of characters that can be present in the widget's input. **/
public const val MIN_LENGTH: Int = 0

/** The limit for the length of the widget's label. **/
public const val LABEL_LENGTH: Int = 45

/** The maximum number of characters that can be present in the wdget's placeholder. **/
public const val PLACEHOLDER_LENGTH: Int = 100

/** An abstract type representing a widget that accepts text from the user. */
public abstract class TextInputWidget<T : TextInputWidget<T>> : Widget<String?>(), KordExKoinComponent {
	private val logger = KotlinLogging.logger { }

	@Suppress("MagicNumber")
	override var width: Int = 5
	override var height: Int = 1
	override var value: String? = null

	/** The [TextInputStyle], to be provided by a subtype. **/
	public abstract val style: TextInputStyle

	/** The widget's label, to be shown on Discord. **/
	public lateinit var label: String

	/** The widget's unique ID on Discord, defaulting to a UUID. **/
	public var id: String = UUID.randomUUID().toString()

	/** The initial value to provide for this widget, if any. **/
	public var initialValue: String? = null

	/** Whether to attempt to translate the initial value. **/
	public var translateInitialValue: Boolean = false

	/** The maximum number of characters that may be provided. **/
	public var maxLength: Int = MAX_LENGTH

	/** The minimum number of characters that may be provided. **/
	public var minLength: Int = MIN_LENGTH

	/** Placeholder text, to be shown to the user on Discord. **/
	public var placeholder: String? = null

	/** Whether this widget must be filled out for the form to be valid. **/
	public var required: Boolean = true

	/** @suppress Translations provider reference, used internally **/
	public val translations: TranslationsProvider by inject()

	public override fun validate() {
		if (this::label.isInitialized.not() || label.isEmpty()) {
			error("Text input widgets must be given a label, but no label was provided.")
		}

		if (label.length > LABEL_LENGTH) {
			logger.debug {
				"Labels must be shorter than $LABEL_LENGTH characters, but ${label.length} characters were provided. " +
					"This may not be a problem if '$label' refers to a translation key, but remember to check that " +
					"none of its translations are too long!"
			}
		}

		@Suppress("UnnecessaryParentheses")
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

	override suspend fun apply(builder: ActionRowBuilder, locale: Locale, bundle: String?) {
		val translatedLabel = translations.translate(label, locale, bundle)

		if (translatedLabel.length > LABEL_LENGTH) {
			error(
				"Labels must be shorter than $LABEL_LENGTH characters, but ${label.length} " +
					"characters were provided. $label -> $translatedLabel"
			)
		}

		builder.textInput(style, id, translatedLabel) {
			this.allowedLength = this@TextInputWidget.minLength..this@TextInputWidget.maxLength
			this.required = this@TextInputWidget.required

			this.placeholder = this@TextInputWidget.placeholder?.let {
				translations.translate(it, locale, bundle)
			}

			this.value = this@TextInputWidget.initialValue?.let {
				if (translateInitialValue) {
					translations.translate(it, locale, bundle)
				} else {
					it
				}
			}
		}
	}

	/** @suppress Internal API method. **/
	@JvmName("setValue1")
	public fun setValue(value: String) {
		this.value = value
	}
}
