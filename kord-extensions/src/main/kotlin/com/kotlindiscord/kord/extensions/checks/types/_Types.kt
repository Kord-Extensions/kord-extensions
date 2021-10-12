@file:Suppress("Filename")

package com.kotlindiscord.kord.extensions.checks.types

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent

/** Types alias representing a check function for a specific event type. **/
public typealias Check<T> = suspend CheckContext<T>.() -> Unit

/** Check type for chat commands. **/
public typealias ChatCommandCheck = Check<MessageCreateEvent>

/** Check type for message commands. **/
public typealias MessageCommandCheck = Check<MessageCommandInteractionCreateEvent>

/** Check type for slash commands. **/
public typealias SlashCommandCheck = Check<ChatInputCommandInteractionCreateEvent>

/** Check type for user commands. **/
public typealias UserCommandCheck = Check<UserCommandInteractionCreateEvent>
