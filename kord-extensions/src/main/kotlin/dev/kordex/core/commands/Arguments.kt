/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands

import dev.kordex.core.annotations.UnexpectedFunctionBehaviour
import dev.kordex.core.commands.converters.*
import dev.kordex.core.i18n.types.Key
import java.util.Locale

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
	@UnexpectedFunctionBehaviour
	public open fun <R : Any> arg(
		displayName: Key,
		description: Key,
		converter: SingleConverter<R>,
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
	@UnexpectedFunctionBehaviour
	public open fun <R : Any> arg(
		displayName: Key,
		description: Key,
		converter: DefaultingConverter<R>,
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
	@UnexpectedFunctionBehaviour
	public open fun <R : Any> arg(
		displayName: Key,
		description: Key,
		converter: OptionalConverter<R>,
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
	@UnexpectedFunctionBehaviour
	public open fun <R : Any> arg(
		displayName: Key,
		description: Key,
		converter: ListConverter<R>,
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
	@UnexpectedFunctionBehaviour
	public open fun <R : Any> arg(
		displayName: Key,
		description: Key,
		converter: CoalescingConverter<R>,
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
	@UnexpectedFunctionBehaviour
	public open fun <R : Any> arg(
		displayName: Key,
		description: Key,
		converter: DefaultingCoalescingConverter<R>,
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
	@UnexpectedFunctionBehaviour
	public open fun <R : Any> arg(
		displayName: Key,
		description: Key,
		converter: OptionalCoalescingConverter<R>,
	): OptionalCoalescingConverter<R> {
		args.add(Argument(displayName, description, converter))

		return converter
	}

	/** Validation function that will throw an error if there's a problem with this Arguments class/subclass. **/
	public open fun validate(locale: Locale) {
		val names: MutableSet<String> = mutableSetOf()

		args.forEach {
			val name = it.displayName.translateLocale(locale).lowercase()

			if (name in names) {
				error("Duplicate argument name/key: ${it.displayName}")
			}

			names.add(name)
		}
	}
}
