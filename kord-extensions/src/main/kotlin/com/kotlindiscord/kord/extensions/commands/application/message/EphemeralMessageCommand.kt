package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.extensions.Extension

/** Ephemeral-followup-only message command. **/
public class EphemeralMessageCommand(
    extension: Extension
) : MessageCommand<EphemeralMessageCommandContext>(extension)
