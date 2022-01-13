/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "UndocumentedPublicProperty",
)  // This file will be removed later

package com.kotlindiscord.kord.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToDefaulting
import com.kotlindiscord.kord.extensions.commands.converters.DefaultingConverter
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.builders.ConverterBuilder
import com.kotlindiscord.kord.extensions.commands.converters.builders.DefaultingConverterBuilder
import com.kotlindiscord.kord.extensions.commands.converters.impl.BooleanConverter
import com.kotlindiscord.kord.extensions.commands.converters.impl.EnumConverter
import com.kotlindiscord.kord.extensions.commands.converters.impl.getEnum

public class DefaultingBooleanConverterBuilder : DefaultingConverterBuilder<Boolean>() {
    @OptIn(ConverterToDefaulting::class)
    public override fun build(arguments: Arguments): DefaultingConverter<Boolean> {
        return arguments.arg(
            displayName = name,
            description = description,

            converter = BooleanConverter().toDefaulting(
                defaultValue = defaultValue,
                outputError = !ignoreErrors,
                nestedValidator = validator
            )
        )
    }
}

public class EnumConverterBuilder<E : Enum<E>>(
    public var getter: suspend (String) -> E?
) : ConverterBuilder<E>() {
    public lateinit var typeName: String
    public var bundle: String? = null

    @OptIn(ConverterToDefaulting::class)
    public override fun build(arguments: Arguments): SingleConverter<E> {
        return arguments.arg(
            displayName = name,
            description = description,

            converter = EnumConverter(
                validator = validator,
                typeName = typeName,
                getter = getter,
                bundle = bundle,
            )
        )
    }
}

public inline fun <reified E : Enum<E>> Arguments.enum(
    body: EnumConverterBuilder<E>.() -> Unit
): SingleConverter<E> {
    val builder = EnumConverterBuilder<E>(
        getter = { getEnum(it) },
    )

    body(builder)

    return builder.build(this)
}

public inline fun Arguments.defaultingBoolean(
    body: DefaultingBooleanConverterBuilder.() -> Unit
): DefaultingConverter<Boolean> {
    val builder = DefaultingBooleanConverterBuilder()

    body(builder)

    return builder.build(this)
}

public class MyArgs : Arguments() {
    public val bool: Boolean by defaultingBoolean {
        name = "bool"
        description = "Boolean argument, defaults to false"

        defaultValue = false

//        autocomplete {
//            "True"
//        }

        validate {
            failIf("Must set the value to `true`.") { !value }
        }
    }
}
