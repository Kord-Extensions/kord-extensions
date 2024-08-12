/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.bot.extensions

import dev.kord.core.behavior.channel.createMessage
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import dev.kordex.core.utils.respond
import dev.kordex.modules.pluralkit.events.*

public class PKTestExtension : Extension() {
	override val name: String = "kordex.test-pluralkit"

	override suspend fun setup() {
		event<ProxiedMessageCreateEvent> {
			action {
				event.message.respond(pingInReply = false) {
					content = "Proxied PK message created: `${event.message.id}`"
				}
			}
		}

		event<ProxiedMessageUpdateEvent> {
			action {
				event.getMessage().respond(pingInReply = false) {
					content = "Proxied PK message updated: `${event.message.id}`"
				}
			}
		}

		event<ProxiedMessageDeleteEvent> {
			action {
				event.channel.createMessage {
					content = "Proxied PK message deleted: `${event.message?.id}`"
				}
			}
		}

		event<UnProxiedMessageCreateEvent> {
			check { failIf(event.message.getAuthorAsMemberOrNull()?.isBot != false) }

			action {
				action {
					event.message.respond(pingInReply = false) {
						content = "Non-proxied message created: `${event.message.id}`"
					}
				}
			}
		}

		event<UnProxiedMessageUpdateEvent> {
			check { failIf(event.message.asMessageOrNull()?.getAuthorAsMemberOrNull()?.isBot != false) }

			action {
				event.getMessage().respond(pingInReply = false) {
					content = "Non-proxied message updated: `${event.message.id}`"
				}
			}
		}

		event<UnProxiedMessageDeleteEvent> {
			check { failIf(event.message?.getAuthorAsMemberOrNull()?.isBot != false) }

			action {
				event.channel.createMessage {
					content = "Non-proxied message deleted: `${event.message?.id}`"
				}
			}
		}
	}
}
