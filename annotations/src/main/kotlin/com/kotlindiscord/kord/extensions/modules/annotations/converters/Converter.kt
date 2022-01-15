/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.annotations.converters

/**
 * Mark the class as a converter class, allowing converter builders and their functions to be generated.
 *
 * @property names Converter names, used to generate the functions. Multiple names will generate multiple sets of
 * functions - surprise surprise.

 * @property types Converter types to generate builders and functions for.
 * @property imports Extra imports required for generated code to be valid.
 *
 * @property builderConstructorArguments Arguments to add to the builder's constructor, if any.
 * @property builderGeneric Generic typevar that the builder should take, if any.
 * @property builderFields Extra fields to add to the builder, if any. Use `lateinit var` for required values.
 *
 * @property functionBuilderArguments Arguments to pass into the builder's constructor, if any.
 * @property functionGeneric Generic typevar that the builder should take, if any. Will be `reified`.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Converter(
    public vararg val names: String,

    public val types: Array<ConverterType>,
    public val imports: Array<String> = [],

    public val builderConstructorArguments: Array<String> = [],
    public val builderGeneric: String = "",
    public val builderFields: Array<String> = [],

    public val functionBuilderArguments: Array<String> = [],
    public val functionGeneric: String = "",
)
