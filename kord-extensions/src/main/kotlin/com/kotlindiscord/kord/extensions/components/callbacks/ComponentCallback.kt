package com.kotlindiscord.kord.extensions.components.callbacks

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.components.ComponentContext
import com.kotlindiscord.kord.extensions.components.buttons.EphemeralInteractionButtonContext
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButtonContext
import com.kotlindiscord.kord.extensions.components.menus.EphemeralSelectMenuContext
import com.kotlindiscord.kord.extensions.components.menus.PublicSelectMenuContext
import com.kotlindiscord.kord.extensions.utils.getLocale
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

/** Sealed class representing a component callback. **/
public sealed class ComponentCallback<C : ComponentContext<*>, E : InteractionCreateEvent> {
    /** @suppress List of checks stored within this callback. **/
    protected open val checkList: MutableList<Check<E>> = mutableListOf()

    /** @suppress Action body, to be called when the component is interacted with. **/
    protected lateinit var body: suspend C.() -> Unit

    /** Call this to supply a callback [body], to be called when the component is interacted with. **/
    public fun action(action: suspend C.() -> Unit) {
        body = action
    }

    /**
     * Define a check which must pass for the callback to be executed.
     *
     * A callback may have multiple checks - all checks must pass for the callback's [body] to be executed.
     * Checks will be run in the order that they're defined.
     *
     * This function can be used DSL-style with a given body, or it can be passed one or more
     * predefined functions. See the samples for more information.
     *
     * @param checks Checks to apply to this command.
     */
    public open fun check(vararg checks: Check<E>) {
        checks.forEach { checkList.add(it) }
    }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to this command.
     */
    public open fun check(check: Check<E>) {
        checkList.add(check)
    }

    /** Runs the checks that are defined for this callback. **/
    @Throws(DiscordRelayedException::class)
    public open suspend fun runChecks(event: E): Boolean {
        val locale = event.getLocale()

        checkList.forEach { check ->
            val context = CheckContext(event, locale)

            check(context)

            if (!context.passed) {
                context.throwIfFailedWithMessage()

                return false
            }
        }

        return true
    }

    /** Function used to run the callback's body. **/
    public open suspend fun call(context: C) {
        body(context)
    }
}

/** Component callback for an ephemeral button. **/
public class EphemeralButtonCallback :
    ComponentCallback<EphemeralInteractionButtonContext, ButtonInteractionCreateEvent>()

/** Component callback for a public button. **/
public class PublicButtonCallback :
    ComponentCallback<PublicInteractionButtonContext, ButtonInteractionCreateEvent>()

/** Component callback for an ephemeral select menu. **/
public class EphemeralMenuCallback :
    ComponentCallback<EphemeralSelectMenuContext, SelectMenuInteractionCreateEvent>()

/** Component callback for a public select menu. **/
public class PublicMenuCallback :
    ComponentCallback<PublicSelectMenuContext, SelectMenuInteractionCreateEvent>()
