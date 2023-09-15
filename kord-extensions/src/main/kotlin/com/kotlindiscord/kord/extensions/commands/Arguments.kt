/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.commands.converters.*

/**
 * Abstract base class for a class containing a set of command arguments.
 *
 * Subclass this, and make use of its extension functions in order to specify the commands for your argument. We
 * recommend placing your subclasses within your Extension classes to avoid polluting your namespaces, but they can
 * be wherever you like - as long as they're public.
 */
public open class Arguments {
    /** List of [Argument] objects, which wrap converters. **/
    public val args: MutableList<Argument<*>> = mutableListOf()

    /**
     * During an autocomplete interaction, whether to try to fill the defined arguments from that event before calling
     * the registered callback.
     *
     * This is only required when you're using a converter that references a previous argument in its autocomplete
     * callback, or you've provided a custom autocomplete callback that does the same thing.
     *
     * When enabled, this will only fill in previous arguments up to the current one.
     * Don't enable this if you don't need it, as it may significantly slow down your bot's autocomplete processing.
     */
    public open val parseForAutocomplete: Boolean = false

    /**
     * Add a [SingleConverter] argument to this set of arguments.
     *
     * This is typically used indirectly, via an extension function that wraps it. It returns the converter, which
     * is intended to be used as a property delegate.
     *
     * @param displayName Display name used in help messages and as the key for keyword arguments.
     * @param converter Converter instance to add.
     *
     * @return Argument converter to use as a delegate.
     */
    public fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: SingleConverter<R>
    ): SingleConverter<R> {
        args.add(Argument(displayName, description, converter))

        return converter
    }

    /**
     * Add a [DefaultingConverter] argument to this set of arguments.
     *
     * This is typically used indirectly, via an extension function that wraps it. It returns the converter, which
     * is intended to be used as a property delegate.
     *
     * @param displayName Display name used in help messages and as the key for keyword arguments.
     * @param converter Converter instance to add.
     *
     * @return Argument converter to use as a delegate.
     */
    public fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: DefaultingConverter<R>
    ): DefaultingConverter<R> {
        args.add(Argument(displayName, description, converter))

        return converter
    }

    /**
     * Add an [OptionalConverter] argument to this set of arguments.
     *
     * This is typically used indirectly, via an extension function that wraps it. It returns the converter, which
     * is intended to be used as a property delegate.
     *
     * @param displayName Display name used in help messages and as the key for keyword arguments.
     * @param converter Converter instance to add.
     *
     * @return Argument converter to use as a delegate.
     */
    public fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: OptionalConverter<R>
    ): OptionalConverter<R> {
        args.add(Argument(displayName, description, converter))

        return converter
    }

    /**
     * Add a [ListConverter] argument to this set of arguments.
     *
     * This is typically used indirectly, via an extension function that wraps it. It returns the converter, which
     * is intended to be used as a property delegate.
     *
     * @param displayName Display name used in help messages and as the key for keyword arguments.
     * @param converter Converter instance to add.
     *
     * @return Argument converter to use as a delegate.
     */
    public fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: ListConverter<R>
    ): ListConverter<R> {
        args.add(Argument(displayName, description, converter))

        return converter
    }

    /**
     * Add a [CoalescingConverter] argument to this set of arguments.
     *
     * This is typically used indirectly, via an extension function that wraps it. It returns the converter, which
     * is intended to be used as a property delegate.
     *
     * @param displayName Display name used in help messages and as the key for keyword arguments.
     * @param converter Converter instance to add.
     *
     * @return Argument converter to use as a delegate.
     */
    public fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: CoalescingConverter<R>
    ): CoalescingConverter<R> {
        args.add(Argument(displayName, description, converter))

        return converter
    }

    /**
     * Add a [DefaultingCoalescingConverter] argument to this set of arguments.
     *
     * This is typically used indirectly, via an extension function that wraps it. It returns the converter, which
     * is intended to be used as a property delegate.
     *
     * @param displayName Display name used in help messages and as the key for keyword arguments.
     * @param converter Converter instance to add.
     *
     * @return Argument converter to use as a delegate.
     */
    public fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: DefaultingCoalescingConverter<R>
    ): DefaultingCoalescingConverter<R> {
        args.add(Argument(displayName, description, converter))

        return converter
    }

    /**
     * Add an [OptionalCoalescingConverter] argument to this set of arguments.
     *
     * This is typically used indirectly, via an extension function that wraps it. It returns the converter, which
     * is intended to be used as a property delegate.
     *
     * @param displayName Display name used in help messages and as the key for keyword arguments.
     * @param converter Converter instance to add.
     *
     * @return Argument converter to use as a delegate.
     */
    public fun <R : Any> arg(
        displayName: String,
        description: String,
        converter: OptionalCoalescingConverter<R>
    ): OptionalCoalescingConverter<R> {
        args.add(Argument(displayName, description, converter))

        return converter
    }

    /** Validation function that will throw an error if there's a problem with this Arguments class/subclass. **/
    public open fun validate() {
        val names: MutableSet<String> = mutableSetOf()

        args.forEach {
            val name = it.displayName.lowercase()

            if (name in names) {
                error("Duplicate argument name: ${it.displayName}")
            }

            names.add(name)
        }
    }
}
