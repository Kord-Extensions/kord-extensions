/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters

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
