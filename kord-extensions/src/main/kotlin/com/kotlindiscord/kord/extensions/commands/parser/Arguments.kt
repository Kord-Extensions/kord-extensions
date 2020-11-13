package com.kotlindiscord.kord.extensions.commands.parser

import com.kotlindiscord.kord.extensions.commands.converters.*

/**
 * Abstract base class for a class containing a set of command arguments.
 *
 * Subclass this, and make use of its extension functions in order to specify the commands for your argument. We
 * recommend placing your subclasses within your Extension classes to avoid polluting your namespaces, but they can
 * be wherever you like - as long as they're public.
 */
open class Arguments {
    /** List of [Argument] objects, which wrap converters. **/
    val args: MutableList<Argument<*>> = mutableListOf()

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
    fun <T : Any> arg(displayName: String, converter: SingleConverter<T>): SingleConverter<T> {
        args.add(Argument(displayName, converter))

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
    fun <T : Any> arg(displayName: String, converter: DefaultingConverter<T>): DefaultingConverter<T> {
        args.add(Argument(displayName, converter))

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
    fun <T : Any?> arg(displayName: String, converter: OptionalConverter<T>): OptionalConverter<T> {
        args.add(Argument(displayName, converter))

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
    fun <T : Any> arg(displayName: String, converter: MultiConverter<T>): MultiConverter<T> {
        args.add(Argument(displayName, converter))

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
    fun <T : Any> arg(displayName: String, converter: CoalescingConverter<T>): CoalescingConverter<T> {
        args.add(Argument(displayName, converter))

        return converter
    }
}
