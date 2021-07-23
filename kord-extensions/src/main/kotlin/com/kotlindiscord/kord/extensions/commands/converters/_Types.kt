@file:Suppress("Filename")

package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Argument

/** Types alias representing a validator callable. Keeps things relatively maintainable. **/
public typealias Validator<T> = (suspend CommandContext.(arg: Argument<*>, value: T) -> Unit)?
