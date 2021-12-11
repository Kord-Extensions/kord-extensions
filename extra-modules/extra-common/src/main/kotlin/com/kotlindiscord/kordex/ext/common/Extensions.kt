/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kordex.ext.common

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kordex.ext.common.builders.ExtCommonBuilder
import com.kotlindiscord.kordex.ext.common.extensions.EmojiExtension

/** Configure the common module and add its extensions to the bot. **/
fun ExtensibleBotBuilder.ExtensionsBuilder.extCommon(builder: ExtCommonBuilder.() -> Unit) {
    if (!EmojiExtension.isConfigured()) {
        val obj = ExtCommonBuilder()

        builder(obj)
        EmojiExtension.configure(obj)
    }

    if (!extCommonAdded) { // Don't add the extensions twice!
        add(::EmojiExtension)
    }

    extCommonAdded = true
}

/** Add the common module's extensions without configuring them. This is intended for use by other extensions. **/
fun ExtensibleBotBuilder.ExtensionsBuilder.addExtCommon() {
    if (!extCommonAdded) { // Don't add the extensions twice!
        add(::EmojiExtension)
    }

    extCommonAdded = true
}

private var extCommonAddedProp: Boolean = false

/** Whether the common extensions have already been added. **/
var ExtensibleBotBuilder.ExtensionsBuilder.extCommonAdded: Boolean
    get() = extCommonAddedProp
    set(value) {
        extCommonAddedProp = value
    }
