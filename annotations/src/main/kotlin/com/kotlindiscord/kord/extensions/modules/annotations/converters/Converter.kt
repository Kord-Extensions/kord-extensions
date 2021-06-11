package com.kotlindiscord.kord.extensions.modules.annotations.converters

/**
 * Mark the class as a converter class, allowing converter functions to be generated.
 *
 * @property name Converter name, used to generate the functions.
 * @property types Converter function types to generate.
 * @property imports Extra imports required for generated code to be valid.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Converter(
    public val name: String,
    public val types: Array<ConverterType>,
    public val imports: Array<String> = []
)
