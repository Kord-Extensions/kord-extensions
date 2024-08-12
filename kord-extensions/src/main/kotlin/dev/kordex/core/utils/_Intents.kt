/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils

import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent

/** Return a capitalised, human-readable string representing what the current intent is for. **/
@OptIn(PrivilegedIntent::class)
public fun Intent.getName(): String = when (this) {
	is Intent.AutoModerationConfiguration -> "Auto Moderation Configuration"
	is Intent.AutoModerationExecution -> "Auto Moderation Execution"
	is Intent.DirectMessageTyping -> "Direct Message Typing"
	is Intent.DirectMessages -> "Direct Messages"
	is Intent.DirectMessagesReactions -> "Direct Messages Reactions"
	is Intent.GuildEmojis -> "Guild Emojis"
	is Intent.GuildIntegrations -> "Guild Integrations"
	is Intent.GuildInvites -> "Guild Invites"
	is Intent.GuildMembers -> "Guild Members"
	is Intent.GuildMessageReactions -> "Guild Message Reactions"
	is Intent.GuildMessageTyping -> "Guild Message Typing"
	is Intent.GuildMessages -> "Guild Messages"
	is Intent.GuildModeration -> "Guild Moderation"
	is Intent.GuildPresences -> "Guild Presences"
	is Intent.GuildScheduledEvents -> "Guild Scheduled Events"
	is Intent.GuildVoiceStates -> "Guild Voice States"
	is Intent.GuildWebhooks -> "Guild Webhooks"
	is Intent.Guilds -> "Guilds"
	is Intent.MessageContent -> "Message Content"
	is Intent.Unknown -> "Unknown"
}
