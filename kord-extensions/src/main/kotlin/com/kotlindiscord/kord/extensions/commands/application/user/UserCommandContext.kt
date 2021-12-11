/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.user

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import dev.kord.core.entity.User
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent

/**
 *  User command context, containing everything you need for your user command's execution.
 *
 *  @param event Event that triggered this message command.
 *  @param command Message command instance.
 */
public abstract class UserCommandContext<C : UserCommandContext<C>>(
    public open val event: UserCommandInteractionCreateEvent,
    public override val command: UserCommand<C>
) : ApplicationCommandContext(event, command) {
    /** Messages that this message command is being executed against. **/
    public val targetUsers: Collection<User> by lazy { event.interaction.users.values }
}
