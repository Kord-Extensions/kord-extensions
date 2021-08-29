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
    public fun <R : Any?> arg(
        displayName: String,
        description: String,
        converter: OptionalConverter<R>
    ): OptionalConverter<R> {
        args.add(Argument(displayName, description, converter))

        return converter
    }

    /**
     * Add a [MultiConverter] argument to this set of arguments.
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
        converter: MultiConverter<R>
    ): MultiConverter<R> {
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
    public fun <R : Any?> arg(
        displayName: String,
        description: String,
        converter: OptionalCoalescingConverter<R>
    ): OptionalCoalescingConverter<R> {
        args.add(Argument(displayName, description, converter))

        return converter
    }
}
