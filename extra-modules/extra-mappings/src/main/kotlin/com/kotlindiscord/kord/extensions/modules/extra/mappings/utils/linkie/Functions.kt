/*
 * Copyright (c) 2019, 2020 shedaniel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kotlindiscord.kord.extensions.modules.extra.mappings.utils.linkie

import me.shedaniel.linkie.Obf

/** Format this obfuscated member as a pair of strings, client to server. **/
fun Obf.stringPairs(): Pair<String?, String?> = when {
    isEmpty() -> "" to null
    isMerged() -> merged!! to null

    else -> client to server
}

/**
 *  If not null or equal to the given string, return the string with the given mapping lambda applied, otherwise null.
 */
inline fun String?.mapIfNotNullOrNotEquals(other: String, mapper: (String) -> String): String? =
    when {
        isNullOrEmpty() -> null
        this == other -> null
        else -> mapper(this)
    }
