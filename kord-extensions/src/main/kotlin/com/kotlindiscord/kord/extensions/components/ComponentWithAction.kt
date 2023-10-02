/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.checks.types.CheckContextWithCache
import com.kotlindiscord.kord.extensions.checks.types.CheckWithCache
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.types.Lockable
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import com.kotlindiscord.kord.extensions.utils.getLocale
import com.kotlindiscord.kord.extensions.utils.permissionsForMember
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import org.koin.core.component.inject

/**
 * Abstract class representing a component with both an ID and executable action.
 *
 * @param E Event type that triggers interaction actions for this component type
 * @param C Context type used for this component's execution context
 * @param M Modal form type representing the ModalForm subtype used, if any.
 *
 * @param timeoutTask Timeout task that will be restarted when [call] is run, if any. This is intended to be used to
 * in the timeout mechanism for the [ComponentContainer] that contains this component.
 *
 * @param modal Callback returning a ModalForm object, probably the constructor of a subtype.
 */
public abstract class ComponentWithAction<
    E : ComponentInteractionCreateEvent,
    C : ComponentContext<*>,
    M : ModalForm,
    >(
    public open val timeoutTask: Task?,
    public open val modal: (() -> M)? = null,
) : ComponentWithID(), Lockable {
    private val logger: KLogger = KotlinLogging.logger {}

    /** @suppress This is only meant for use by code that extends the command system. **/
    public val componentRegistry: ComponentRegistry by inject()

    /** Whether to use a deferred ack, which will prevent Discord's "Thinking..." message. **/
    public open var deferredAck: Boolean = true

    /** @suppress **/
    public open val checkList: MutableList<CheckWithCache<E>> = mutableListOf()

    /** Bot permissions required to be able to run execute this component's action. **/
    public open val requiredPerms: MutableSet<Permission> = mutableSetOf()

    public override var locking: Boolean = false

    override var mutex: Mutex? = null

    /** Component body, to be called when the component is interacted with. **/
    public lateinit var body: suspend C.(M?) -> Unit

    /** Call this to supply a component [body], to be called when the component is interacted with. **/
    public fun action(action: suspend C.(M?) -> Unit) {
        body = action
    }

    /**
     * Define a check which must pass for the component's body to be executed.
     *
     * A component may have multiple checks - all checks must pass for the component's body to be executed.
     * Checks will be run in the order that they're defined.
     *
     * This function can be used DSL-style with a given body, or it can be passed one or more
     * predefined functions. See the samples for more information.
     *
     * @param checks Checks to apply to this command.
     */
    public open fun check(vararg checks: CheckWithCache<E>) {
        checks.forEach { checkList.add(it) }
    }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to this command.
     */
    public open fun check(check: CheckWithCache<E>) {
        checkList.add(check)
    }

    override fun validate() {
        super.validate()

        if (!::body.isInitialized) {
            error("No component body given.")
        }

        if (locking && mutex == null) {
            mutex = Mutex()
        }
    }

    /** If your bot requires permissions to be able to execute this component's body, add them using this function. **/
    public fun requireBotPermissions(vararg perms: Permission) {
        perms.forEach(requiredPerms::add)
    }

    /** Runs standard checks that can be handled in a generic way, without worrying about subclass-specific checks. **/
    @Throws(DiscordRelayedException::class)
    public open suspend fun runStandardChecks(event: E, cache: MutableStringKeyedMap<Any>): Boolean {
        val locale = event.getLocale()

        checkList.forEach { check ->
            val context = CheckContextWithCache(event, locale, cache)

            check(context)

            if (!context.passed) {
                context.throwIfFailedWithMessage()

                return false
            }
        }

        return true
    }

    /** Override this in order to implement any subclass-specific checks. **/
    @Throws(DiscordRelayedException::class)
    public open suspend fun runChecks(event: E, cache: MutableStringKeyedMap<Any>): Boolean =
        runStandardChecks(event, cache)

    /** Checks whether the bot has the specified required permissions, throwing if it doesn't. **/
    @Throws(DiscordRelayedException::class)
    public open suspend fun checkBotPerms(context: C) {
        if (requiredPerms.isEmpty()) {
            return  // Nothing to check, don't try to hit the cache
        }

        if (context.guild != null) {
            val perms = context
                .getChannel()
                .asChannelOf<GuildChannel>()
                .permissionsForMember(kord.selfId)

            val missingPerms = requiredPerms.filter { !perms.contains(it) }

            if (missingPerms.isNotEmpty()) {
                throw DiscordRelayedException(
                    context.translate(
                        "commands.error.missingBotPermissions",
                        null,

                        replacements = arrayOf(
                            missingPerms
                                .map { it.translate(context.getLocale()) }
                                .joinToString()
                        )
                    )
                )
            }
        }
    }

    /** Override this to implement your component's calling logic. Check subtypes for examples! **/
    public open suspend fun call(event: E) {
        timeoutTask?.restart()
    }
}
