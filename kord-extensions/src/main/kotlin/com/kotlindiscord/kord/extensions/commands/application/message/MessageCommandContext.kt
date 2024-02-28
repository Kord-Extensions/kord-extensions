/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.entity.Message
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent

/**
 *  Message command context, containing everything you need for your message command's execution.
 *
 *  @param event Event that triggered this message command.
 *  @param command Message command instance.
 */
public abstract class MessageCommandContext<C : MessageCommandContext<C, M>, M : ModalForm>(
	public open val event: MessageCommandInteractionCreateEvent,
	public override val command: MessageCommand<C, M>,
	cache: MutableStringKeyedMap<Any>,
) : ApplicationCommandContext(event, command, cache) {
	/** Messages that this message command is being executed against. **/
	public val targetMessages: Collection<Message> by lazy { event.interaction.messages.values }
}
