# Extensions

Extensions represent defined units of functionality. They provide a way for you to group event handling and command
logic together into logical blocks that can be managed individually from each other.

While it is technically possible to create commands and event handlers without extensions, we recommend extensions
for all use-cases. If you have a use-case that extensions don't cover, please let us know.

Extensions support the following features:

* String name references and public APIs
* Loading/unloading at runtime, with custom setup/teardown logic
* Registered event handlers with custom event support
* Standalone commands with rich metadata
* Infinitely nestable command groups

## Extension basics

When writing an extension, the first thing you'll need to do is extend the `Extension` class and implement its
abstract members - the `name` property and `setup` function.

```kotlin
class TextExtension(bot: ExtensibleBot) : Extension(bot) {
    // The name this extension will be referred to by
    override val name: String = "test"
    
    override suspend fun setup() {
        // The majority of our extension logic goes here
    }
}
```

Once we've created our extension object, we'll need to register it to our [bot object](/concepts/bot).

```kotlin
val bot = ExtensibleBot(token, prefix)

suspend fun main() {
    bot.addExtension(::TestExtension)

    bot.login()
}
```

Our basic extension is complete - it'll be loaded up and functional by the time the bot has connected to Discord.
That said, it's a bit boring - let's try adding some functionality to it. There are three common behavioural patterns
your extension is likely to implement.

## Reacting to events

As an example, say we wanted to reply to users that say "ping" with "pong". For this contrived example, we could
write an event handler that reacts to message creation events.

```kotlin
// Within your extension class...
override suspend fun setup() {
    event<MessageCreateEvent> {
        action {
            if (event.message.content.equals("ping", true)) {
                event.message.respond("pong")
            }
        }
    }
}
```

??? note "Custom events"

    This is not limited to Kord events - pretty much any object can be an event, although we recommend extending
    `ExtensionEvent` for your own events. To send an event, you can use `ExtensibleBot#send(event)`.

For more information on working with events, please see [the events page](/concepts/events).

## Reacting to commands

Commands are one of the most common methods of interaction with any Discord bot. For this reason, Kord Extensions
provides a comprehensive, rich commands framework. For example, we could rewrite the ping example above as a command
instead.

```kotlin
override suspend fun setup() {
    command {
        name = "ping"

        action {
            message.respond("pong")
        }
    }
}
```

??? note "Further functionality"

    The commands framework is a deep, rich and feature-filled system that aims to support bot developers by making
    it as simple as possible to define commands that validate themselves and automatically parse their arguments.
    
    It also provides a set of classes that can be extended in order to further customize the behaviour of the
    commands system.

??? important "The future of commands"

    Parts of the commands framework are in the middle of being rewritten. That said, the commands system should be
    stable between versions - that is to say, we'll increment the version number before breaking it.
    
    We're hoping to expand upon this system in future, starting with a command dispatcher system that allows you to
    specify additional command dispatchers with their own prefixes and sets of commands, and switch out the argument
    parser used. This is a long way off, though, so we recommend keeping an eye on the GitHub repository if you're
    waiting for this.

For more information on working with commands, please see [the commands page](/concepts/commands).

## Polling and relaying

For some bots, you'll need to work with external services. A common use-case is polling an external service and
posting changes to a Discord channel, which you can do with Kotlin's Jobs. For this example, we'll write a quick
extension that polls something every 30 seconds, and sends a message to a channel with updates.

```kotlin
// 30 seconds in milliseconds
private const val INTERVAL = 1000L * 30L

// 10 seconds in milliseconds - use this if you need to wait for
// guild members (for example) to be populated.
private const val SETUP_DELAY = 1000L * 10L

class CheckExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "check"

    private var data: String = ""  // ...or whatever
    private var job: Job? = null

    override suspend fun setup() {
        // Iniital setup should always happen after the ReadyEvent. This
        // is because the ReadyEvent handler has some special logic.
        // You can read more about that on the events page.
        event<ReadyEvent> {
            action {
                delay(SETUP_DELAY)

                // Launch a job in Kord's coroutine scope
                job = bot.kord.launch {
                    while (true) {
                        delay(INTERVAL)

                        val changed = doCheck()  // Whatever it is

                        if (changed) {
                            getChannel().createMessage("Data updated: $data")
                        }
                    }
                }
            }
        }
    }

    override suspend fun unload() {
        // This will be called if the extension is unloaded at any point,
        // we should stop the job if that happens.
        job?.cancel()
    }
}
```

The important thing to note here is that we're launching the job in Kord's coroutine scope, which gives us a `Job`
object we can keep track of. While extensions won't be automatically unloaded, extensions are able to load and
unload each other, and all extensions are expected to support this behaviour.

If you can't support unloading in your extension for some reason, remember to override `unload()` and throw an 
exception!

## Class Members

The `Extension` class exposes several useful members, and some internal ones. Because we can't possibly know exactly
what you need from this system, we don't hide the functional parts - but the below table will mark the things you
probably don't need with a :wrench:.

### Properties

Name   | Type     | Description
:----- | :------: | :----------
`name` | `String` | The name of the extension, which is how it'll be referred to throughout the bot.
`eventHandlers` | `MutableList <EventHandler>` | :wrench:{: title="Intended for internal use." } List of event handlers registered to this extension.
`commands` | `MutableList <Command>` | :wrench:{: title="Intended for internal use." } List of commands registered to this extension.
`loaded` | `Boolean` | :wrench:{: title="Intended for internal use." } Whether the extension is currently loaded - this is set automatically.

### Functions

Name | Description
:--- | :----------
`command` | Create a new command for this extension.
`event` | Create a new event handler for this extension.
`group` | Create a new grouped command for this extension (a command that can have subcommands).
`setup` | Override this and add all of your setup logic for the extension.
`unload` | Override this if you need to clean up when your extension is unloaded.
`doSetup` | :wrench:{: title="Intended for internal use." } Called by the bot when setting up the extension, firing events and handling extra setup tasks.
`doUnload` | :wrench:{: title="Intended for internal use." } Called by the bot when unloading the extension, firing events, unregistering event handlers and commands and handling extra cleanup.
