package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import me.shedaniel.linkie.Namespace

/** An argument base which provides an argument for a mapping channel. **/
@Suppress("UndocumentedPublicProperty")
abstract class MappingWithChannelArguments(namespace: Namespace) : MappingArguments(namespace) {
    abstract val channel: ChoiceEnum?
}
