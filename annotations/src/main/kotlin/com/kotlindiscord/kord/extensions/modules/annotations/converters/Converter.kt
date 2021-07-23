package com.kotlindiscord.kord.extensions.modules.annotations.converters

/**
 * Mark the class as a converter class, allowing converter functions to be generated.
 *
 * @property names Converter names, used to generate the functions. Multiple names will generate multiple set of
 * functions - surprise surprise.

 * @property types Converter function types to generate.
 * @property imports Extra imports required for generated code to be valid.
 *
 * @property arguments Extra argument lines to add to every generated function, without the trailing comma. These will
 * be added as extra function arguments, and they'll also be passed into the class as matching named arguments- so
 * your converter's constructor must have the same names!
 *
 * @property generic Generic typevar to be made accessible in your converter functions. The function will be marked
 * inline and the typevar will be reified - if you have custom callable arguments, you'll probably need to mark them
 * `noinline`. Typevars should be specified without the angle brackets - eg, "T : Any".
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Converter(
    public vararg val names: String,
    public val types: Array<ConverterType>,
    public val imports: Array<String> = [],
    public val arguments: Array<String> = [],
    public val generic: String = ""
)
