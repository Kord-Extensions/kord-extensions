/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent

/** Public-only message command context. **/
public class PublicMessageCommandContext(
    override val event: MessageCommandInteractionCreateEvent,
    override val command: MessageCommand<PublicMessageCommandContext>,
    override val interactionResponse: PublicInteractionResponseBehavior
) : MessageCommandContext<PublicMessageCommandContext>(event, command), PublicInteractionContext
