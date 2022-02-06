/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.unsafe.contexts

import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommandContext
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.types.UnsafeInteractionContext
import dev.kord.core.behavior.interaction.InteractionResponseBehavior
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent

/** Command context for an unsafe user command. **/
@UnsafeAPI
public class UnsafeUserCommandContext(
    override val event: UserCommandInteractionCreateEvent,
    override val command: UserCommand<UnsafeUserCommandContext>,
    override var interactionResponse: InteractionResponseBehavior?
) : UserCommandContext<UnsafeUserCommandContext>(event, command), UnsafeInteractionContext
