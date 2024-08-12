/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.annotations.converters

/**
 * Mark the class as a converter class, allowing converter builders and their functions to be generated.
 *
 * @property names Converter names, used to generate the functions. Multiple names will generate multiple sets of
 * functions - surprise surprise.

 * @property types Converter types to generate builders and functions for.
 * @property imports Extra imports required for generated code to be valid.
 *
 * @property builderConstructorArguments Arguments to add to the builder's constructor, if any. Prefix with "!!" to
 * stop the argument from being passed into the converter.
 *
 * @property builderGeneric Generic typevar that the builder should take, if any.
 * @property builderFields Extra fields to add to the builder, if any. Use `lateinit var` for required values.
 * @property builderBuildFunctionPreStatements Extra lines of code to add to the builder's `build()` function,
 * before the converter is instantiated.
 * @property builderBuildFunctionStatements Extra lines of code to add to the builder's `build()` function,
 * after the converter is instantiated.
 * @property builderInitStatements Extra lines of code to add to the builder's `init { }` block.
 * @property builderExtraStatements Extra lines of code to add to the builder, after the fields.
 * @property builderSuffixedWhere Extra generic bounds to place after `where` in the builder's signature.
 *
 * @property functionBuilderArguments Arguments to pass into the builder's constructor, if any.
 * @property functionGeneric Generic typevar that the function should take, if any. Will be `reified`.
 * @property functionSuffixedWhere Extra generic bounds to place after `where` in the function's signature.
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
    public val builderSuffixedWhere: String = "",

    public val builderBuildFunctionPreStatements: Array<String> = [],
    public val builderBuildFunctionStatements: Array<String> = [],
    public val builderInitStatements: Array<String> = [],
    public val builderExtraStatements: Array<String> = [],

    public val functionBuilderArguments: Array<String> = [],
    public val functionGeneric: String = "",
    public val functionSuffixedWhere: String = "",
)
