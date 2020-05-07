package com.kotlindiscord.kord.extensions

import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.events.EventHandler
import com.kotlindiscord.kord.extensions.extensions.Extension
import kotlin.reflect.KClass

/**
 * A base class for all custom exceptions in our bot framework.
 */
open class ExtensionsException : Exception()

/**
 * Thrown when an attempt to load an [Extension] fails.
 *
 * @param clazz The invalid [Extension] class.
 * @param reason Why this [Extension] is considered invalid.
 */
class InvalidExtensionException(val clazz: KClass<out Extension>, val reason: String?) : ExtensionsException() {
    override fun toString(): String {
        val formattedReason = if (reason != null) {
            " ($reason)"
        } else {
            ""
        }

        return "Invalid extension class: ${clazz.qualifiedName} $formattedReason"
    }
}

/**
 * Thrown when an [EventHandler] could not be validated.
 *
 * @param reason Why this [EventHandler] is considered invalid.
 */
class InvalidEventHandlerException(val reason: String) : ExtensionsException() {
    override fun toString(): String = "Invalid event handler: $reason"
}

/**
 * Thrown when an attempt to register a [EventHandler] fails.
 *
 * @param reason Why this [EventHandler] could not be registered.
 */
class EventHandlerRegistrationException(val reason: String) : ExtensionsException() {
    override fun toString(): String = "Failed to register event handler: $reason"
}

/**
 * Thrown when a [Command] could not be validated.
 *
 * @param name The [Command] name
 * @param reason Why this [Command] is considered invalid.
 */
class InvalidCommandException(val name: String?, val reason: String) : ExtensionsException() {
    override fun toString(): String {
        if (name == null) {
            return "Invalid command: $reason"
        }

        return "Invalid command $name: $reason"
    }
}

/**
 * Thrown when an attempt to register a [Command] fails.
 *
 * @param name The [Command] name
 * @param reason Why this [Command] could not be registered.
 */
class CommandRegistrationException(val name: String?, val reason: String) : ExtensionsException() {
    override fun toString(): String {
        if (name == null) {
            return "Failed to register command: $reason"
        }

        return "Failed to register command $name: $reason"
    }
}
