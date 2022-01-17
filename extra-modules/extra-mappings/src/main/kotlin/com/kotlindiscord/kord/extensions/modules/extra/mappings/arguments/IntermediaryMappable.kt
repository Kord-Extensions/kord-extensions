package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

/**
 * Indicates that a namespace can map field types
 * and method descriptors to intermediary names.
 */
interface IntermediaryMappable {
    /**
     * Whether the results should map to named instead of intermediary/hashed.
     */
    val mapDescriptors: Boolean
}
