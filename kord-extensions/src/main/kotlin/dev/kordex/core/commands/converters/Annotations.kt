/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters

@RequiresOptIn(
	message = "When creating an Arguments class, you must use one of the converter functions starting with " +
		"`defaulting` instead. Otherwise, if you know what you're doing (or you're writing your own converter " +
		"functions), please feel free to opt-in.\n\n" +

		"For example, instead of `boolean(...).toDefaulting()`, consider `defaultingBoolean(...)`.\n\n" +

		"Failure to register your converters this way may result in strange or broken behaviour."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
/** Opt-in annotation for .toDefaulting() converter functions. **/
public annotation class ConverterToDefaulting

@RequiresOptIn(
	message = "When creating an Arguments class, you must use one of the converter functions ending with " +
		"\"List\" instead. Otherwise, if you know what you're doing (or you're writing your own converter " +
		"functions), please feel free to opt-in.\n\n" +

		"For example, instead of `boolean(...).toMulti()`, consider `booleanList(...)`.\n\n" +

		"Failure to register your converters this way may result in strange or broken behaviour."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
/** Opt-in annotation for .toMulti() converter functions. **/
public annotation class ConverterToMulti

@RequiresOptIn(
	message = "When creating an Arguments class, you must use one of the converter functions starting with " +
		"\"optional\" instead. Otherwise, if you know what you're doing (or you're writing your own converter " +
		"functions), please feel free to opt-in.\n\n" +

		"For example, instead of `boolean(...).toOptional()`, consider `optionalBoolean(...)`.\n\n" +

		"Failure to register your converters this way may result in strange or broken behaviour."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
/** Opt-in annotation for .toOptional() converter functions. **/
public annotation class ConverterToOptional
