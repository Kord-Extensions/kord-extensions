/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("Filename")

package dev.kordex.core.checks.types

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent

/** Types alias representing a check function for a specific event type. **/
public typealias Check<T> = suspend CheckContext<T>.() -> Unit

/** Types alias representing a check function for a specific event type. **/
public typealias CheckWithCache<T> = suspend CheckContextWithCache<T>.() -> Unit

/** Check type for chat commands. **/
public typealias ChatCommandCheck = CheckWithCache<MessageCreateEvent>

/** Check type for message commands. **/
public typealias MessageCommandCheck = CheckWithCache<MessageCommandInteractionCreateEvent>

/** Check type for slash commands. **/
public typealias SlashCommandCheck = CheckWithCache<ChatInputCommandInteractionCreateEvent>

/** Check type for user commands. **/
public typealias UserCommandCheck = CheckWithCache<UserCommandInteractionCreateEvent>
