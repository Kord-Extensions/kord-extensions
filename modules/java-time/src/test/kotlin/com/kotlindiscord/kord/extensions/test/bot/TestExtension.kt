package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.modules.time.java.coalescedJ8Duration
import com.kotlindiscord.kord.extensions.modules.time.java.toHuman
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.annotation.KordPreview

@OptIn(KordPreview::class)
@Suppress("UnderscoresInNumericLiterals")  // They're IDs
class TestExtension : Extension() {
    override val name = "test"

    class TestArgs : Arguments() {
        val duration by coalescedJ8Duration(
            "duration",
            "Duration argument",
            shouldThrow = true
        )
    }

    override suspend fun setup() {
        command(::TestArgs) {
            name = "format"
            description = "Let's test formatting."

            action {
                message.respond(
                    arguments.duration.toHuman(this) ?: "Empty duration!"
                )
            }
        }
    }
}
