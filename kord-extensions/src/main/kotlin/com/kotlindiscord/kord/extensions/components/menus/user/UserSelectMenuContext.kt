/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus.user

import com.kotlindiscord.kord.extensions.components.Component
import com.kotlindiscord.kord.extensions.components.menus.SelectMenuContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

/** Abstract class representing the execution context of a user select (dropdown) menu component. **/
public abstract class UserSelectMenuContext(
    component: Component,
    event: SelectMenuInteractionCreateEvent,
    cache: MutableStringKeyedMap<Any>,
) : SelectMenuContext(component, event, cache) {
    /** Menu options that were selected by the user before de-focusing the menu. **/
    @OptIn(KordUnsafe::class, KordExperimental::class)
    public val selected: List<UserBehavior> by lazy {
        event.interaction.values.map { event.kord.unsafe.user(Snowflake(it)) }
    }
}
