<%--
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this
 file, You can obtain one at https://mozilla.org/MPL/2.0/.
--%>

@import java.util.List

@param String packageName
@param String qualifiedClassName
@param String qualifiedTypeName

@param List<String> imports

@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package ${packageName}

// Original converter class, for safety
import ${qualifiedClassName}

// Imports that all converters need
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.Arguments
import dev.kord.common.annotation.KordPreview

@if(qualifiedTypeName != null)
    // Converter type param
    import ${qualifiedTypeName}
@endif

@for(String importName: imports)
    import ${importName}
@endfor
