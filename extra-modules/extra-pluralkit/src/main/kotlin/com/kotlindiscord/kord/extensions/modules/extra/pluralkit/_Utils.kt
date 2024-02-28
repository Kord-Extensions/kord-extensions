/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.pluralkit

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder

/** Set up and add the PluralKit extension to your bot. **/
fun ExtensibleBotBuilder.ExtensionsBuilder.extPluralKit() {
	add(::PKExtension)
}
