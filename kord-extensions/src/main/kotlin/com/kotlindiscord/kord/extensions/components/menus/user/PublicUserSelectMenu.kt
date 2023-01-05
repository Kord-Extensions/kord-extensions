/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus.user

import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.components.menus.PublicSelectMenu
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder

public typealias InitialPublicSelectMenuResponseBuilder =
    (suspend InteractionResponseCreateBuilder.(SelectMenuInteractionCreateEvent) -> Unit)?

/** Class representing a public-only user select (dropdown) menu. **/
public open class PublicUserSelectMenu<M : ModalForm>(
    timeoutTask: Task?,
    public override val modal: (() -> M)? = null,
) : PublicSelectMenu<PublicUserSelectMenuContext<M>, M>(timeoutTask), UserSelectMenu {
    override fun createContext(
        event: SelectMenuInteractionCreateEvent,
        interactionResponse: PublicMessageInteractionResponseBehavior,
        cache: MutableStringKeyedMap<Any>,
    ): PublicUserSelectMenuContext<M> = PublicUserSelectMenuContext(
        this, event, interactionResponse, cache
    )

    override fun apply(builder: ActionRowBuilder): Unit = applyUserSelectMenu(this, builder)
}
