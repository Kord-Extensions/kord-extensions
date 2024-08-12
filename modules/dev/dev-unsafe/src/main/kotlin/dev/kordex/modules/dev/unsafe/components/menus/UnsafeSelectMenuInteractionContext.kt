/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.modules.dev.unsafe.components.menus

import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.UnsafeComponentInteractionContext

/** Interface representing a generic, unsafe interaction action context. **/
@UnsafeAPI
public interface UnsafeSelectMenuInteractionContext :
	UnsafeComponentInteractionContext<SelectMenuInteractionCreateEvent>
