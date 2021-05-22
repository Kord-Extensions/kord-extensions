package com.kotlindiscord.kordex.ext.common.builders

import com.kotlindiscord.kordex.ext.common.configuration.emoji.EmojiConfig
import com.kotlindiscord.kordex.ext.common.configuration.emoji.TomlEmojiConfig

/** Builder used for configuring the extensions bundled with the common module. **/
class ExtCommonBuilder {
    /** Config adapter to use to load the emoji extension configuration. **/
    var emojiConfig: EmojiConfig = TomlEmojiConfig()
}
