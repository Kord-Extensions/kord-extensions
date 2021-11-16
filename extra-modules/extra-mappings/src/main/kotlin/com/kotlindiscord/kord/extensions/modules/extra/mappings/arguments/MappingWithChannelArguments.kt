package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import me.shedaniel.linkie.Namespace

abstract class MappingWithChannelArguments(namespace: Namespace) : MappingArguments(namespace) {
    abstract val channel: ChoiceEnum?
}