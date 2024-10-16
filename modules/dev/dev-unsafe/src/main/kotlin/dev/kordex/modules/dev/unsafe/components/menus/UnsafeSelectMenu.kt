/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */
package dev.kordex.modules.dev.unsafe.components.menus

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.components.menus.SelectMenu
import dev.kordex.core.components.menus.SelectMenuContext
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale
import dev.kordex.core.utils.scheduling.Task
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm

@OptIn(UnsafeAPI::class)
public abstract class UnsafeSelectMenu<C, M : UnsafeModalForm>(
	timeoutTask: Task?,
	public override val modal: (() -> M)? = null,
) : SelectMenu<C, M>(timeoutTask) where C : SelectMenuContext, C : UnsafeSelectMenuInteractionContext {
	/** Initial response type. Change this to decide what happens when this menu's action is executed. **/
	public var initialResponse: InitialInteractionSelectMenuResponse = InitialInteractionSelectMenuResponse.EphemeralAck

	override fun validate() {
		super.validate()

		if (modal != null && initialResponse != InitialInteractionSelectMenuResponse.None) {
			error("You may not provide a modal builder and an initial response - pick one, not both.")
		}
	}

	@OptIn(KordUnsafe::class, InternalAPI::class)
	@Suppress("TooGenericExceptionCaught")
	override suspend fun call(event: SelectMenuInteractionCreateEvent): Unit = withLock {
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
			is InitialInteractionSelectMenuResponse.EphemeralAck -> event.interaction.deferEphemeralResponseUnsafe()
			is InitialInteractionSelectMenuResponse.PublicAck -> event.interaction.deferPublicResponseUnsafe()

			is InitialInteractionSelectMenuResponse.EphemeralResponse -> event.interaction.respondEphemeral {
				r.builder!!(event)
			}

			is InitialInteractionSelectMenuResponse.PublicResponse -> event.interaction.respondPublic {
				r.builder!!(event)
			}

			is InitialInteractionSelectMenuResponse.None -> if (modalObj != null) {
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

					modalObj.respond(it)
				} ?: return@withLock
			} else {
				null
			}
		}

		val context = createContext(event, response, cache)

		context.populate()

		firstSentryBreadcrumb(context, this)

		try {
			checkBotPerms(context)
		} catch (e: DiscordRelayedException) {
			respondText(context, e.reason, FailureReason.OwnPermissionsCheckFailure(e))

			return@withLock
		}

		try {
			body(context, modalObj)
		} catch (e: DiscordRelayedException) {
			respondText(context, e.reason, FailureReason.RelayedFailure(e))
		} catch (t: Throwable) {
			handleError(context, t, this)
		}
	}

	override suspend fun respondText(
		context: C,
		message: Key,
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

	/** Function to create the context of the select menu. **/
	public abstract fun createContext(
		event: SelectMenuInteractionCreateEvent,
		interactionResponse: MessageInteractionResponseBehavior?,
		cache: MutableStringKeyedMap<Any>,
	): C
}
