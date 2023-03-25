/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("UncheckedCast")

package com.kotlindiscord.kord.extensions.modules.annotations.converters

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import com.kotlindiscord.kord.extensions.modules.annotations.orNull

/**
 * Class representing the arguments that are defined within a converter annotation, extracted from its declaration.
 *
 * @property annotation Annotation definition to extract data from.
 */
@Suppress("UNCHECKED_CAST")
public data class ConverterAnnotationArgs(public val annotation: KSAnnotation) {
    /** @suppress **/
    private val argMap: Map<String?, Any?> =
        annotation.arguments
            .associate { it.name?.getShortName() to it.value }
            .filterKeys { it != null }

    /** @suppress **/
    public val names: ArrayList<String> =
        argMap["names"] as ArrayList<String>? ?: arrayListOf()

    /** @suppress **/
    public val types: List<ConverterType> =
        (argMap["types"]!! as ArrayList<KSType>).mapNotNull {
            ConverterType.fromName(it.declaration.simpleName.asString())
        }

    /** @suppress **/
    public val imports: ArrayList<String> =
        argMap["imports"] as ArrayList<String>? ?: arrayListOf()

    /** @suppress **/
    public val builderConstructorArguments: ArrayList<String> =
        argMap["builderConstructorArguments"] as ArrayList<String>? ?: arrayListOf()

    /** @suppress **/
    public val builderGeneric: String? =
        (argMap["builderGeneric"] as String).orNull()

    /** @suppress **/
    public val builderFields: ArrayList<String> =
        argMap["builderFields"] as ArrayList<String>? ?: arrayListOf()

    /** @suppress **/
    public val builderBuildFunctionStatements: ArrayList<String> =
        argMap["builderBuildFunctionStatements"] as ArrayList<String>? ?: arrayListOf()

    /** @suppress **/
    public val builderExtraStatements: ArrayList<String> =
        argMap["builderExtraStatements"] as ArrayList<String>? ?: arrayListOf()

    /** @suppress **/
    public val builderInitStatements: ArrayList<String> =
        argMap["builderInitStatements"] as ArrayList<String>? ?: arrayListOf()

    /** @suppress **/
    public val builderSuffixedWhere: String? =
        (argMap["builderSuffixedWhere"] as String).orNull()

    /** @suppress **/
    public val functionGeneric: String? =
        (argMap["functionGeneric"] as String).orNull()

    /** @suppress **/
    public val functionBuilderArguments: ArrayList<String> =
        argMap["functionBuilderArguments"] as ArrayList<String>? ?: arrayListOf()

    /** @suppress **/
    public val functionSuffixedWhere: String? =
        (argMap["functionSuffixedWhere"] as String).orNull()
}
