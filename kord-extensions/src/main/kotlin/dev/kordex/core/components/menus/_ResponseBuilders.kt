/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.menus

import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder

public typealias InitialEphemeralSelectMenuResponseBuilder =
	(suspend InteractionResponseCreateBuilder.(SelectMenuInteractionCreateEvent) -> Unit)?

public typealias InitialPublicSelectMenuResponseBuilder =
	(suspend InteractionResponseCreateBuilder.(SelectMenuInteractionCreateEvent) -> Unit)?
