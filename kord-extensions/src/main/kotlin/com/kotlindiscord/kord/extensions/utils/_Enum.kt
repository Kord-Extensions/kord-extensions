@file:JvmMultifileClass
@file:JvmName("EnumKt")

package com.kotlindiscord.kord.extensions.utils

import kotlin.reflect.*

/**
 * Retrieve an enum by his name.
 * @receiver Enum in which the search will process.
 * @param name Name of enum searched.
 * @return The enum value if the name is found, null otherwise.
 */
public fun <T : Enum<*>> KClass<T>.findByName(name: String, ignoreCase: Boolean = true): T? {
    return if (name.isEmpty()) {
        null
    } else java.enumConstants.find { it.name.equals(name, ignoreCase) }
}
