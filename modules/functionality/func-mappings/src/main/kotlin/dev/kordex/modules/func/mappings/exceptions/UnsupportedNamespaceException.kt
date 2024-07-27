/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings.exceptions

/**
 * Thrown when an unsupported namespace is configured.
 *
 * @property namespace The invalid namespace.
 **/
class UnsupportedNamespaceException(val namespace: String) : Exception(
	"Unknown/unsupported namespace: $namespace"
)
