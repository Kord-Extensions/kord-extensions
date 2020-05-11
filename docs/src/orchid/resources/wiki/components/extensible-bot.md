### Extensible Bot

The `ExtensibleBot` class is the entry point for our framework. It's in charge of managing your 
[extensions](./extension), and keeping track of [commands](./command).

To get started, you'll need to create an instance of `ExtensibleBot`. You can also subclass it if you need to add
or change functionality. For example:

```kotlin
package com.kotlindiscord.bot

import com.kotlindiscord.bot.config.config
import com.kotlindiscord.bot.extensions.TestExtension
import com.kotlindiscord.bot.extensions.VerificationExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot

/** 
 * The current instance of the bot.
 *
 * It's often better to use named parameters if you need to be specific.
 * Both parameters take the same type, after all!
 */
val bot = ExtensibleBot(prefix = config.prefix, token = config.token)

/**
 * The main function. Every story has a beginning!
 *
 * @param args Array of command-line arguments. These are ignored.
 */
suspend fun main(args: Array<String>) {
    bot.addExtension(TestExtension::class)
    bot.addExtension(VerificationExtension::class)
    bot.start()
}
```

For more information on the public API for `ExtensibleBot`, feel free to take a look at the API docs.
