/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.events.*
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.core.behavior.channel.createMessage

public class PKTestExtension : Extension() {
    override val name: String = "test-pluralkit"

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
            check { failIf(event.message.getAuthorAsMember()?.isBot != false) }

            action {
                action {
                    event.message.respond(pingInReply = false) {
                        content = "Non-proxied message created: `${event.message.id}`"
                    }
                }
            }
        }

        event<UnProxiedMessageUpdateEvent> {
            check { failIf(event.message.asMessageOrNull()?.getAuthorAsMember()?.isBot != false) }

            action {
                event.getMessage().respond(pingInReply = false) {
                    content = "Non-proxied message updated: `${event.message.id}`"
                }
            }
        }

        event<UnProxiedMessageDeleteEvent> {
            check { failIf(event.message?.getAuthorAsMember()?.isBot != false) }

            action {
                event.channel.createMessage {
                    content = "Non-proxied message deleted: `${event.message?.id}`"
                }
            }
        }
    }
}
