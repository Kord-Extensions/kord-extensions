/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.events

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent

// region Invocation events

/** Basic event emitted when am application command is invoked. **/
public interface ApplicationCommandInvocationEvent<
    C : ApplicationCommand<*>,
    E : ApplicationCommandInteractionCreateEvent
> : CommandInvocationEvent<C, E>
