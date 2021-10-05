package com.kotlindiscord.kord.extensions.components.callbacks

import com.kotlindiscord.kord.extensions.components.ComponentContext
import com.kotlindiscord.kord.extensions.components.buttons.EphemeralInteractionButtonContext
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButtonContext
import com.kotlindiscord.kord.extensions.components.menus.EphemeralSelectMenuContext
import com.kotlindiscord.kord.extensions.components.menus.PublicSelectMenuContext

/** Functional interface representing a component callback. **/
public fun interface ComponentCallback<ContextType : ComponentContext<*>> {
    /** Function used to invoke the callback. **/
    public fun ContextType.invoke()
}

/** Component callback for an ephemeral button. **/
public fun interface EphemeralButtonCallback : ComponentCallback<EphemeralInteractionButtonContext>

/** Component callback for a public button. **/
public fun interface PublicButtonCallback : ComponentCallback<PublicInteractionButtonContext>

/** Component callback for an ephemeral select menu. **/
public fun interface EphemeralMenuCallback : ComponentCallback<EphemeralSelectMenuContext>

/** Component callback for a public select menu. **/
public fun interface PublicMenuCallback : ComponentCallback<PublicSelectMenuContext>
