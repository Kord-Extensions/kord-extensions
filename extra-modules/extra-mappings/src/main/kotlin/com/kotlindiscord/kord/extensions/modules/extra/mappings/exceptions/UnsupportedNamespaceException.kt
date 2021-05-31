package com.kotlindiscord.kord.extensions.modules.extra.mappings.exceptions

/**
 * Thrown when an unsupported namespace is configured.
 *
 * @property namespace The invalid namespace.
 **/
class UnsupportedNamespaceException(val namespace: String) : Exception(
    "Unknown/unsupported namespace: $namespace"
)
