/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(KordUnsafe::class)

package dev.kordex.core.components.forms

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.ModalParentInteractionBehavior
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.entity.interaction.ModalSubmitInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.interaction.ModalSubmitInteractionCreateEvent
import dev.kord.rest.builder.interaction.ModalBuilder
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.components.ComponentContext
import dev.kordex.core.components.ComponentRegistry
import dev.kordex.core.components.forms.widgets.LineTextWidget
import dev.kordex.core.components.forms.widgets.ParagraphTextWidget
import dev.kordex.core.components.forms.widgets.TextInputWidget
import dev.kordex.core.components.forms.widgets.Widget
import dev.kordex.core.events.EventContext
import dev.kordex.core.events.ModalInteractionCompleteEvent
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.utils.waitFor
import org.koin.core.component.inject
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Class representing a modal form.
 *
 * This should be extended by classes representing individual modals.
 */
public abstract class ModalForm : Form(), KordExKoinComponent {
	/** The modal's title, shown on Discord. **/
	public abstract var title: Key

	/** @suppress Internal reference. **/
	protected val bot: ExtensibleBot by inject()

	/** @suppress Internal reference. **/
	protected val componentRegistry: ComponentRegistry by inject()

	override val timeout: Duration = 15.minutes

	/** ID representing this modal on Discord. **/
	public var id: String = UUID.randomUUID().toString()

	/** A widget representing a single-line text input. **/
	public fun lineText(
		coordinate: CoordinatePair? = null,
		builder: LineTextWidget.() -> Unit,
	): LineTextWidget {
		val widget = LineTextWidget()

		builder(widget)
		widget.validate()

		grid.setAtCoordinateOrFirstRow(coordinate, widget)

		return widget
	}

	/** A widget representing a multi-line paragraph input. **/
	public fun paragraphText(
		coordinate: CoordinatePair? = null,
		builder: ParagraphTextWidget.() -> Unit,
	): ParagraphTextWidget {
		val widget = ParagraphTextWidget()

		builder(widget)
		widget.validate()

		grid.setAtCoordinateOrFirstRow(coordinate, widget)

		return widget
	}

	/** @suppress Internal function called by the component registry. **/
	public suspend fun call(event: ModalSubmitInteractionCreateEvent) {
		grid.filter { it.isNotEmpty() }
			.forEach { row ->
				row.filterNotNull()
					.forEach { widget ->
						val textInput = widget as TextInputWidget<*>
						val value = event.interaction.textInputs[textInput.id]?.value

						if (value != null) {
							textInput.setValue(value)
						}
					}
			}

		bot.send(
			ModalInteractionCompleteEvent(
				id,
				event.interaction
			)
		)
	}

	/** Given a ModalBuilder, apply this modal's widgets for display on Discord. **/
	public suspend fun applyToBuilder(builder: ModalBuilder, locale: Locale) {
		val appliedWidgets = mutableSetOf<Widget<*>>()

		grid.forEach { row ->
			val filteredRow = row.filterNotNull()
				.filter { it !in appliedWidgets }

			if (filteredRow.isNotEmpty()) {
				builder.actionRow {
					filteredRow.forEach { widget ->
						if (widget !in appliedWidgets) {
							widget.apply(this, locale)
							appliedWidgets.add(widget)
						}
					}
				}
			}
		}
	}

	/** Wait for this modal to be completed and call the [callback]. Parameter will be `null` if timed out. **/
	public suspend fun <T : Any?> awaitCompletion(callback: suspend (ModalSubmitInteraction?) -> T): T {
		val completionEvent = bot.waitFor<ModalInteractionCompleteEvent>(timeout) { id == this@ModalForm.id }

		return callback(completionEvent?.interaction)
	}

	/** Return a translated modal title using the given locale. **/
	public fun translateTitle(locale: Locale): String =
		title
			.withLocale(locale)
			.translate()

	/**
	 * Convenience function to send this modal to the given [interaction] and await its completion, running the provided
	 * [callback].
	 *
	 * `null` will be provided to the callback if the modal times out before the user responds.
	 *
	 * More specific convenience functions are available, such as [sendAndDeferEphemeral] and [sendAndDeferPublic].
	 */
	public suspend fun <T : Any?> sendAndAwait(
		locale: Locale,
		interaction: ModalParentInteractionBehavior,
		callback: suspend (ModalSubmitInteraction?) -> T,
	): T {
		componentRegistry.register(this)

		interaction.modal(translateTitle(locale), id) {
			applyToBuilder(this, locale)
		}

		return awaitCompletion(callback)
	}

	/**
	 * Convenience function that calls the basic [sendAndAwait] function with parameters taken from the current event
	 * context.
	 *
	 * `null` will be provided to the callback if the modal times out before the user responds.
	 */
	public suspend fun <T : Any?, E : InteractionCreateEvent> sendAndAwait(
		context: EventContext<E>,
		callback: suspend (ModalSubmitInteraction?) -> T,
	): T {
		val interaction = context.event.interaction as? ModalParentInteractionBehavior
			?: error("Interaction ${context.event.interaction} does not support responding with a modal.")

		return sendAndAwait(context.getLocale(), interaction, callback)
	}

	/**
	 * Convenience function that calls the basic [sendAndAwait] function with parameters taken from the current command
	 * context.
	 *
	 * `null` will be provided to the callback if the modal times out before the user responds.
	 */
	public suspend fun <T : Any?> sendAndAwait(
		context: dev.kordex.core.commands.application.ApplicationCommandContext,
		callback: suspend (ModalSubmitInteraction?) -> T,
	): T {
		val interaction = context.genericEvent.interaction as? ModalParentInteractionBehavior
			?: error("Interaction ${context.genericEvent.interaction} does not support responding with a modal.")

		return sendAndAwait(context.getLocale(), interaction, callback)
	}

	/**
	 * Convenience function that calls the basic [sendAndAwait] function with parameters taken from the current
	 * component context.
	 *
	 * `null` will be provided to the callback if the modal times out before the user responds.
	 */
	public suspend fun <T : Any?> sendAndAwait(
		context: ComponentContext<*>,
		callback: suspend (ModalSubmitInteraction?) -> T,
	): T {
		val interaction = context.event.interaction as? ModalParentInteractionBehavior
			?: error("Interaction ${context.event.interaction} does not support responding with a modal.")

		return sendAndAwait(context.getLocale(), interaction, callback)
	}

	/**
	 * Convenience function that sends the modal, awaits its completion, and returns a deferred ephemeral interaction
	 * response.
	 *
	 * Returns `null` if the modal times out before the user responds.
	 */
	public suspend fun <E : InteractionCreateEvent> sendAndDeferEphemeral(
		context: EventContext<E>,
	): EphemeralMessageInteractionResponseBehavior? = sendAndAwait(context) {
		it?.deferEphemeralResponseUnsafe()
	}

	/**
	 * Convenience function that sends the modal, awaits its completion, and returns a deferred ephemeral interaction
	 * response.
	 *
	 * Returns `null` if the modal times out before the user responds.
	 */
	public suspend fun sendAndDeferEphemeral(
		context: dev.kordex.core.commands.application.ApplicationCommandContext,
	): EphemeralMessageInteractionResponseBehavior? = sendAndAwait(context) {
		it?.deferEphemeralResponseUnsafe()
	}

	/**
	 * Convenience function that sends the modal, awaits its completion, and returns a deferred ephemeral interaction
	 * response.
	 *
	 * Returns `null` if the modal times out before the user responds.
	 */
	public suspend fun sendAndDeferEphemeral(
		context: ComponentContext<*>,
	): EphemeralMessageInteractionResponseBehavior? = sendAndAwait(context) {
		it?.deferEphemeralResponseUnsafe()
	}

	/**
	 * Convenience function that sends the modal, awaits its completion, and returns a deferred public interaction
	 * response.
	 *
	 * Returns `null` if the modal times out before the user responds.
	 */
	public suspend fun <E : InteractionCreateEvent> sendAndDeferPublic(
		context: EventContext<E>,
	): PublicMessageInteractionResponseBehavior? = sendAndAwait(context) {
		it?.deferPublicResponseUnsafe()
	}

	/**
	 * Convenience function that sends the modal, awaits its completion, and returns a deferred public interaction
	 * response.
	 *
	 * Returns `null` if the modal times out before the user responds.
	 */
	public suspend fun sendAndDeferPublic(
		context: dev.kordex.core.commands.application.ApplicationCommandContext,
	): PublicMessageInteractionResponseBehavior? = sendAndAwait(context) {
		it?.deferPublicResponseUnsafe()
	}

	/**
	 * Convenience function that sends the modal, awaits its completion, and returns a deferred public interaction
	 * response.
	 *
	 * Returns `null` if the modal times out before the user responds.
	 */
	public suspend fun sendAndDeferPublic(
		context: ComponentContext<*>,
	): PublicMessageInteractionResponseBehavior? = sendAndAwait(context) {
		it?.deferPublicResponseUnsafe()
	}
}
