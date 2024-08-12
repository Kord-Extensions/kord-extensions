/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")
@file:OptIn(KordUnsafe::class)

package dev.kordex.modules.dev.unsafe.components.buttons

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.UnsafeComponentInteractionContext

/** Interface representing a generic, unsafe interaction action context. **/
@UnsafeAPI
public interface UnsafeButtonInteractionContext :
	UnsafeComponentInteractionContext<ButtonInteractionCreateEvent>
