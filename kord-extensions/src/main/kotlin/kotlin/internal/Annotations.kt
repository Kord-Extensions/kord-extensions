/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("MatchingDeclarationName")

package kotlin.internal

/**
 * The value of this type parameter should be mentioned in input types (argument types, receiver type or expected type).
 */
@Target(AnnotationTarget.TYPE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
public annotation class OnlyInputTypes
