/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.dev.unsafe.components.forms

import dev.kord.core.entity.interaction.ModalSubmitInteraction
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder

public typealias InitialEphemeralModalResponseBuilder =
	(suspend InteractionResponseCreateBuilder.(ModalSubmitInteraction) -> Unit)?

public typealias InitialPublicModalResponseBuilder =
	(suspend InteractionResponseCreateBuilder.(ModalSubmitInteraction) -> Unit)?
