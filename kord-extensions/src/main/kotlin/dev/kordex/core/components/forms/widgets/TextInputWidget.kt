/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.forms.widgets

import dev.kord.common.entity.TextInputStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kordex.core.i18n.EMPTY_VALUE_STRING
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.validator.GenericValidator.maxLength
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
	public lateinit var label: Key

	/** The widget's unique ID on Discord, defaulting to a UUID. **/
	public var id: String = UUID.randomUUID().toString()

	/** The initial value to provide for this widget, if any. **/
	public var initialValue: Key? = null

	/** The maximum number of characters that may be provided. **/
	public var maxLength: Int = MAX_LENGTH

	/** The minimum number of characters that may be provided. **/
	public var minLength: Int = MIN_LENGTH

	/** Placeholder text, to be shown to the user on Discord. **/
	public var placeholder: Key? = null

	/** Whether this widget must be filled out for the form to be valid. **/
	public var required: Boolean = true

	/** Whether to translate the [initialValue]. **/
	public var translateInitialValue: Boolean = false

	public override fun validate() {
		if (this::label.isInitialized.not() || label.key.isEmpty()) {
			error("Text input widgets must be given a label, but no label was provided.")
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
	}

	override suspend fun apply(builder: ActionRowBuilder, locale: Locale) {
		val translatedLabel = label
			.withLocale(locale)
			.translate()

		val translatedPlaceholder = placeholder
			?.withLocale(locale)
			?.translate()

		val translatedInitialValue = if (translateInitialValue) {
			initialValue
				?.withLocale(locale)
				?.translate()
				?.let {
					if (it == EMPTY_VALUE_STRING) {
						null
					} else {
						it
					}
				}
		} else {
			initialValue?.key
		}

		if (translatedLabel.length > LABEL_LENGTH) {
			error(
				"Labels must be shorter than $LABEL_LENGTH characters, but ${translatedLabel.length} " +
					"characters were provided. $label -> $translatedLabel"
			)
		}

		if (
			translatedPlaceholder != null &&
			(translatedPlaceholder.length > PLACEHOLDER_LENGTH || translatedPlaceholder.isEmpty())
		) {
			error(
				"Invalid value for placeholder provided (${translatedPlaceholder.length} characters) - expected " +
					"${MIN_LENGTH + 1} - $PLACEHOLDER_LENGTH characters"
			)
		}

		if (
			translatedInitialValue != null &&
			(translatedInitialValue.length > MAX_LENGTH || translatedInitialValue.isEmpty())
		) {
			error(
				"Invalid value for initial value provided (${translatedInitialValue.length} characters) - expected " +
					"${MIN_LENGTH + 1} - $MAX_LENGTH characters"
			)
		}

		builder.textInput(style, id, translatedLabel) {
			this.allowedLength = this@TextInputWidget.minLength..this@TextInputWidget.maxLength
			this.required = this@TextInputWidget.required

			this.placeholder = translatedPlaceholder
			this.value = translatedInitialValue
		}
	}

	/** @suppress Internal API method. **/
	@JvmName("setValue1")
	public fun setValue(value: String) {
		this.value = value
	}
}
