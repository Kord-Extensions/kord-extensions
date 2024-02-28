/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events

import dev.kord.core.entity.interaction.ModalSubmitInteraction

/**
 * Event fired when a modal interaction has completed. Used by commands to figure out when to run their actions.
 *
 * @param id Unique ID for the modal form.
 * @param interaction Interaction object provided by the corresponding event.
 */
public class ModalInteractionCompleteEvent(
	public val id: String,
	public val interaction: ModalSubmitInteraction,
) : KordExEvent
