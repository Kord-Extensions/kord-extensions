package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.extensions.Extension

/** Public-followup-only message command. **/
public class PublicMessageCommand(
    extension: Extension
) : MessageCommand<PublicMessageCommandContext>(extension)
