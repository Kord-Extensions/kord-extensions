@file:Suppress("Filename")

package com.kotlindiscord.kord.extensions.checks.types

/** Types alias representing a check function for a specific event type. **/
public typealias Check<T> = suspend CheckContext<T>.() -> Unit
