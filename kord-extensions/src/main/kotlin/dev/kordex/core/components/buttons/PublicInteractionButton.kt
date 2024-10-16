/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("TooGenericExceptionCaught")
@file:OptIn(KordUnsafe::class)

package dev.kordex.core.components.buttons

import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale
import dev.kordex.core.utils.scheduling.Task

public typealias InitialPublicButtonResponseBuilder =
	(suspend InteractionResponseCreateBuilder.(ButtonInteractionCreateEvent) -> Unit)?

/** Class representing a public-only button component. **/
public open class PublicInteractionButton<M : ModalForm>(
	timeoutTask: Task?,
	public override val modal: (() -> M)? = null,
) : InteractionButtonWithAction<PublicInteractionButtonContext<M>, M>(timeoutTask) {
	/** Button style - anything but Link is valid. **/
	public open var style: ButtonStyle = ButtonStyle.Primary

	/** @suppress Initial response builder. **/
	public open var initialResponseBuilder: InitialPublicButtonResponseBuilder = null

	/** Call this to open with a response, omit it to ack instead. **/
	public fun initialResponse(body: InitialPublicButtonResponseBuilder) {
		initialResponseBuilder = body
	}

	override fun apply(builder: ActionRowBuilder) {
		builder.interactionButton(style, id) {
			emoji = partialEmoji
			label = this@PublicInteractionButton.label?.translate()

			disabled = this@PublicInteractionButton.disabled
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
				settings.failureResponseBuilder(
					this,

					e.reason.withLocale(event.getLocale()),

					FailureReason.ProvidedCheckFailure(e)
				)
			}

			return@withLock
		}

		val modalObj = modal?.invoke()

		val response = if (initialResponseBuilder != null) {
			event.interaction.respondPublic { initialResponseBuilder!!(event) }
		} else if (modalObj != null) {
			componentRegistry.register(modalObj)

			val locale = event.getLocale()

			event.interaction.modal(
				modalObj.translateTitle(locale),
				modalObj.id
			) {
				modalObj.applyToBuilder(this, event.getLocale())
			}

			modalObj.awaitCompletion {
				componentRegistry.unregisterModal(modalObj)

				if (!deferredAck) {
					it?.deferPublicResponseUnsafe()
				} else {
					it?.deferPublicMessageUpdate()
				}
			} ?: return@withLock
		} else {
			if (!deferredAck) {
				event.interaction.deferPublicResponseUnsafe()
			} else {
				event.interaction.deferPublicMessageUpdate()
			}
		}

		val context = PublicInteractionButtonContext(this, event, response, cache)

		context.populate()

		firstSentryBreadcrumb(context, this)

		try {
			checkBotPerms(context)
		} catch (e: DiscordRelayedException) {
			respondText(
				context,
				e.reason.withLocale(context.getLocale()),
				FailureReason.OwnPermissionsCheckFailure(e)
			)

			return@withLock
		}

		try {
			body(context, modalObj)
		} catch (e: DiscordRelayedException) {
			respondText(
				context,
				e.reason.withLocale(context.getLocale()),
				FailureReason.RelayedFailure(e)
			)
		} catch (t: Throwable) {
			handleError(context, t, this)
		}
	}

	override fun validate() {
		super.validate()

		if (modal != null && initialResponseBuilder != null) {
			error("You may not provide a modal builder and an initial response - pick one, not both.")
		}

		if (style == ButtonStyle.Link) {
			error("The Link button style is reserved for link buttons.")
		}
	}

	override suspend fun respondText(
		context: PublicInteractionButtonContext<M>,
		message: Key,
		failureType: FailureReason<*>,
	) {
		context.respond { settings.failureResponseBuilder(this, message, failureType) }
	}
}
