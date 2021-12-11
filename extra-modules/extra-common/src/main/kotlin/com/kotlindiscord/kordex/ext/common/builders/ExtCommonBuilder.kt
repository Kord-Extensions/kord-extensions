/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kordex.ext.common.builders

import com.kotlindiscord.kordex.ext.common.configuration.emoji.EmojiConfig
import com.kotlindiscord.kordex.ext.common.configuration.emoji.TomlEmojiConfig

/** Builder used for configuring the extensions bundled with the common module. **/
class ExtCommonBuilder {
    /** Config adapter to use to load the emoji extension configuration. **/
    var emojiConfig: EmojiConfig = TomlEmojiConfig()
}
