package com.kotlindiscord.kord.extensions.utils

/** Object with easy access to unusual mentionable sections of Discord. **/
public object Mentions {
    /** A clickable mention that takes you to this server's home tab. **/
    public const val GuildHome: String = "<id:home>"

    /** A clickable mention that takes you to this server's "browse channels" tab. **/
    public const val GuildBrowseChannels: String = "<id:browse>"

    /** A clickable mention that takes you to this server's "customize community"/onboarding tab. **/
    public const val GuildCustomizeCommunity: String = "<id:customize>"
}
