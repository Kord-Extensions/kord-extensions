package com.kotlindiscord.kord.extensions.utils

import java.util.*

/**
 * Check whether this [Optional] is present.
 *
 * @return `true` if the optional is not present (AKA absent), `false` otherwise.
 */
fun Optional<*>.isAbsent() = this.isPresent.not()

/**
 * Get the value from an [Optional] if it's present, otherwise return `null`.
 */
fun <T> Optional<T>.getOrNull(): T? {
    if (this.isAbsent()) return null

    return this.get()
}
