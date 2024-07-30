/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.modules.dev.unsafe.components.menus

import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.UnsafeComponentInteractionContext

/** Interface representing a generic, unsafe interaction action context. **/
@UnsafeAPI
public interface UnsafeSelectMenuInteractionContext :
	UnsafeComponentInteractionContext<SelectMenuInteractionCreateEvent>
