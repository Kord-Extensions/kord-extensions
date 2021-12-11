/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.annotations.converters

/**
 * Enum representing different types of converter functions.
 */
public enum class ConverterType {
    CHOICE,
    COALESCING,
    DEFAULTING,
    LIST,
    OPTIONAL,
    SINGLE,
}
