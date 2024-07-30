/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.dev.unsafe.components.buttons

import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.components.buttons.InteractionButtonWithAction
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale
import dev.kordex.core.utils.scheduling.Task
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.contexts.UnsafeInteractionComponentContext

@OptIn(KordUnsafe::class)
@UnsafeAPI
public open class UnsafeInteractionButton<M : ModalForm>(
	timeoutTask: Task?,
	public override val modal: (() -> M)? = null,
) : InteractionButtonWithAction<UnsafeInteractionComponentContext<M>, M>(timeoutTask) {
	/** Button style - anything but Link is valid. **/
	public open var style: ButtonStyle = ButtonStyle.Primary

	/** Initial response type. Change this to decide what happens when this button's action is executed. **/
	public var initialResponse: InitialInteractionButtonResponse = InitialInteractionButtonResponse.EphemeralAck

	override fun apply(builder: ActionRowBuilder) {
		builder.interactionButton(style, id) {
			emoji = partialEmoji
			label = this@UnsafeInteractionButton.label

			disabled = this@UnsafeInteractionButton.disabled
		}
	}

	override suspend fun call(event: ButtonInteractionCreateEvent): Unit = withLock {
		val cache: MutableStringKeyedMap<Any> = mutableMapOf()

		super.call(event)

		try {
			if (!runChecks(event, cache)) {
				return@withLock
			}
		} catch (e: DiscordRelayedException) {
			event.interaction.respondEphemeral {
				settings.failureResponseBuilder(this, e.reason, FailureReason.ProvidedCheckFailure(e))
			}

			return@withLock
		}

		val modalObj = modal?.invoke()

		val response = when (val r = initialResponse) {
			is InitialInteractionButtonResponse.EphemeralAck -> event.interaction.deferEphemeralResponseUnsafe()
			is InitialInteractionButtonResponse.PublicAck -> event.interaction.deferPublicResponseUnsafe()

			is InitialInteractionButtonResponse.EphemeralResponse -> event.interaction.respondEphemeral {
				r.builder!!(event)
			}

			is InitialInteractionButtonResponse.PublicResponse -> event.interaction.respondPublic {
				r.builder!!(event)
			}

			is InitialInteractionButtonResponse.None -> if (modalObj != null) {
				componentRegistry.register(modalObj)

				val locale = event.getLocale()

				event.interaction.modal(
					modalObj.translateTitle(locale, bundle),
					modalObj.id
				) {
					modalObj.applyToBuilder(this, event.getLocale(), bundle)
				}

				modalObj.awaitCompletion {
					componentRegistry.unregisterModal(modalObj)

					if (!deferredAck) {
						it?.deferEphemeralResponseUnsafe()
					} else {
						it?.deferEphemeralMessageUpdate()
					}
				} ?: return@withLock
			} else {
				null
			}
		}

		val context = UnsafeInteractionComponentContext(this, event, response, cache)

		context.populate()

		firstSentryBreadcrumb(context, this)

		try {
			checkBotPerms(context)
		} catch (e: DiscordRelayedException) {
			respondText(context, e.reason, FailureReason.OwnPermissionsCheckFailure(e))

			return@withLock
		}

		@Suppress("TooGenericExceptionCaught")
		try {
			body(context, modalObj)
		} catch (e: DiscordRelayedException) {
			respondText(context, e.reason, FailureReason.RelayedFailure(e))
		} catch (t: Throwable) {
			handleError(context, t, this)
		}
	}

	override fun validate() {
		super.validate()

		if (modal != null && initialResponse != InitialInteractionButtonResponse.None) {
			error("You may not provide a modal builder and an initial response - pick one, not both.")
		}

		if (style == ButtonStyle.Link) {
			error("The Link button style is reserved for link buttons.")
		}
	}

	override suspend fun respondText(
		context: UnsafeInteractionComponentContext<M>,
		message: String,
		failureType: FailureReason<*>,
	) {
		when (context.interactionResponse) {
			is PublicMessageInteractionResponseBehavior -> context.respondPublic {
				settings.failureResponseBuilder(this, message, failureType)
			}

			is EphemeralMessageInteractionResponseBehavior -> context.respondEphemeral {
				settings.failureResponseBuilder(this, message, failureType)
			}

			null -> context.ackEphemeral {
				settings.failureResponseBuilder(this, message, failureType)
			}
		}
	}
}
