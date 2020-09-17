### Event Handlers

Event handlers function similarly to [commands](./command), but they react to Kord events instead of command
invocations on Discord. For a full list of Kord events, you can 
[check out the Kord GitHub](https://github.com/kordlib/kord/-/tree/master/core/src/main/kotlin/com/gitlab/kordlib/core/event)
\- but for the purposes of this page, we'll just be looking at a `MessageCreateEvent` - an event that is fired when
someone sends a message.

#### Defining Event Handlers

Define your event handlers in your extension's `setup` function. Here's a basic example:

```kotlin
override suspend fun setup() {
    event<MessageCreateEvent> {  // this: EventHandler
        action {  // it: MessageCreateEvent
            with(it) {
                message.channel.createMessage("Thanks for your message, ${message.author!!.mention}")
            }
        }
    }
}
```

#### Options

All event handlers require an `action`. The `action` is the body of the event handler - when Kord fires the
corresponding event, this will be executed.

Additionally, just like commands, event handlers support checks.

#### Checks

You will likely want to write event handlers that are only executed based on a set of conditions.
In order to facilitate this in a manner that allows you to easily reuse these conditions,
event handlers support a [checks system](check).

```kotlin
suspend fun defaultCheck(event: MessageCreateEvent): Boolean {
    with(event) {
        return when {
            message.author?.id == bot.kord.getSelf().id -> false  // Check that we didn't send this message.
            message.author?.isBot == true               -> false  // Check that another bot didn't send this message.
            else                                        -> true
        }
    }
}

// ...

event<MessageCreateEvent> {
    check(::defaultCheck)

    action {
        with(it) {
            message.channel.createMessage("Thanks for your message, ${message.author!!.mention}")
        }
    }
}
```

You can have as many checks as you need - simply call `check` again for every check you wish to add. If
you have multiple check functions to add, you may also specify them all as arguments for a single `check` call.

```kotlin
check(
    ::defaultCheck,  // Default checks we do for every event
    notChannelType(ChannelType.DM)  // Ensure the event isn't being fired for a DM
)
```

**Note:** Most of the [bundled checks](check) only support `MessageCreateEvent` events. If you need to
support other events, feel free to write your own checks and submit them in a pull request!
