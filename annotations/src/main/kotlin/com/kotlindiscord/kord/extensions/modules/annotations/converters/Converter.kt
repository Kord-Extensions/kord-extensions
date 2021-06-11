package com.kotlindiscord.kord.extensions.modules.annotations.converters

/**
 * Mark the class as a converter class, allowing converter functions to be generated.
 *
 * @property name Converter name, used to generate the functions.
 * @property types Converter function types to generate.
 * @property imports Extra imports required for generated code to be valid.
 *
 * @property arguments Extra argument lines to add to every generated function, without the trailing comma. These will
 * be added as extra function arguments, and they'll also be passed into the class as matching named arguments- so
 * your converter's constructor must have the same names!
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Converter(
    public val name: String,
    public val types: Array<ConverterType>,
    public val imports: Array<String> = [],
    public val arguments: Array<String> = []
)
