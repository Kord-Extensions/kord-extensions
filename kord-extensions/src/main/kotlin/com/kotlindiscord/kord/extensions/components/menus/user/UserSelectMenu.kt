package com.kotlindiscord.kord.extensions.components.menus.user

import com.kotlindiscord.kord.extensions.components.menus.OPTIONS_MAX
import com.kotlindiscord.kord.extensions.components.menus.SelectMenu
import dev.kord.rest.builder.component.ActionRowBuilder

/** Interface for user select menus. **/
public interface UserSelectMenu {

    /** Apply the user select menu to an action row builder. **/
    public fun applyUserSelectMenu(selectMenu: SelectMenu<*, *>, builder: ActionRowBuilder) {
        if (selectMenu.maximumChoices == null) selectMenu.maximumChoices = OPTIONS_MAX

        builder.userSelect(selectMenu.id) {
            this.allowedValues = selectMenu.minimumChoices..selectMenu.maximumChoices!!
            this.placeholder = selectMenu.placeholder
            this.disabled = selectMenu.disabled
        }
    }
}
