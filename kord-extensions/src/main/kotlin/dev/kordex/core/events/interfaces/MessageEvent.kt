/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events.interfaces

import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.entity.Message

/** Generic interface for custom events that can contain message behaviors. Mostly used by checks. **/
public interface MessageEvent {
	/** The message behavior for this event, if any. **/
	public val message: MessageBehavior?

	/** Get a Message object, or throw if one can't be retrieved. **/
	public suspend fun getMessage(): Message

	/** Get a Message object, or return null if one can't be retrieved. **/
	public suspend fun getMessageOrNull(): Message?
}
