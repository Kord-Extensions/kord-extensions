@file:OptIn(ConverterToOptional::class)

package com.kotlindiscord.kord.extensions.modules.extra.mappings.converters

import com.kotlindiscord.kord.extensions.commands.converters.ConverterToOptional
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.Namespace

/** Optional mappings version converter; see KordEx bundled functions for more info. **/
fun Arguments.optionalMappingsVersion(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    namespace: Namespace,
    validator: (suspend Argument<*>.(MappingsContainer?) -> Unit)? = null
) =
    arg(
        displayName,
        description,
        MappingsVersionConverter({ namespace })
            .toOptional(outputError = outputError, nestedValidator = validator)
    )
