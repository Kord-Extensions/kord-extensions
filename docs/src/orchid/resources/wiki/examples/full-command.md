### Full Command Example

The below is a sample extension implementing a command, which makes use of and explains
all the options available to you.

```kotlin
package com.kotlindiscord.kord.extensions.samples

import dev.kord.common.entity.ChannelType
import dev.kord.core.entity.User
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.notChannelType
import com.kotlindiscord.kord.extensions.extensions.Extension

class CommandFullSample(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "command-action-sample"

    override suspend fun setup() {
        /**
         * A data class representing the arguments for the command we'll define below.
         *
         * You don't have to define this in the setup function, it's just placed here for this
         * example.
         *
         * Arguments are attempted to be filled in the order they're defined in the data class'
         * primary constructor. If an argument has a default value then it's considered optional,
         * so it will be skipped if type conversion fails.
         *
         * Typed [List] arguments are filled using the rest of the command's arguments, so most
         * of the time you'll only want to provide a list as the last argument for a command.
         *
         * For more information on how this works, please check out the examples in the wiki.
         */
        data class SampleArgs(
            val optionalInt: Int = 1,  // An optional integer
            val requiredUsers: List<User>  // A list of User objects, to be converted from IDs 
                                           // or mentions
        )

        val sampleCommand = command {  // You can optionally store the command object yourself 
                                       // if you need it elsewhere.
            aliases = arrayOf("test", "hello")  // Alternative names that can be used for 
                                                // command invocation.
            description = "A sample command. Outputs `Hello, world!'."  // Description for help 
                                                                        // command.
            enabled = true  // Whether the command is enabled; this can be changed at runtime 
                            // as well.
            hidden = false  // Whether to hide the command from help command listings.
            name = "sample"  // The name of the command.

            signature<SampleArgs>()  // Generate the signature from the given dataclass.
            signature = "[optionalInt] <requiredUsers ...>"  // Manually set the command's 
                                                             // signature instead.

            check(notChannelType(ChannelType.DM))  // Check that the message wasn't sent to us 
                                                   // in a DM.
            check {
                // Check that the message wasn't sent by a bot.
                it.message.author?.isBot == false
            }

            action { // The body of the command, executed assuming the checks all pass.
                with(parse<SampleArgs>()) {  // Automatically parse command arguments into the
                                             // given data class.
                    message.channel.createMessage("Hello, world!")

                    // Since we're in a `with` block, we can directly use the parameters from 
                    // the data class.
                    message.channel.createMessage(
                        "Arguments: optionalInt = $optionalInt | " +
                            "requiredUsers = ${requiredUsers.joinToString(", ") { it.username }}"
                    )
                }
            }
        }
    }
}
```
