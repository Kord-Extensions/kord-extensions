### Full Event Handler Example

The below is a sample extension implementing an event handler, which makes use of and explains
all the options available to you.

```kotlin
package com.kotlindiscord.kord.extensions.samples

import com.gitlab.kordlib.common.entity.ChannelType
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.notChannelType
import com.kotlindiscord.kord.extensions.extensions.Extension

class EventHandlerSample(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "event-handler-sample"

    override suspend fun setup() {
        val eventHandler = event<MessageCreateEvent> {  // Define an event handler, optionally storing it if needed.
            // This one listens for new messages.
            check(notChannelType(ChannelType.DM))  // Check that the message wasn't sent to us
            // in a DM.
            check {
                // Check that the message wasn't sent by a bot.
                it.message.author?.isBot == false
            }

            check {
                it.message.author != null  // Ensure the message has an author (webhooks and some system messages don't)
            }

            action {  // The body of the event handler, executed assuming the checks all pass.
                with(it) {  // The event object
                    // We know the author isn't null because of our check earlier!
                    message.channel.createMessage("Thanks for your message, ${message.author!!.mention}")
                }
            }
        }
    }
}
```
