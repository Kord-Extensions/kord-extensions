### Checks

The checks system provides a light filtering system for [commands](command) and [event handlers](event-handler).
We've provided a set of general-purpose checks as part of the framework, to cover most general cases - but you're
always able to write your own as well.

#### Writing Your Own Checks

While we've already covered writing custom checks inline in your commands and event handlers, often it'll be
more useful to define these checks somewhere that they can be re-used.

Every check is simply a suspended function that takes a Kord event object and returns a `Boolean`. If the function
returns `true` then the check has passed and processing can continue. If it returns `false` then the check has failed
and processing stops at that point.

You can define your check as a simple function. For example:

```kotlin
suspend fun isNotBot(event: MessageCreateEvent): Boolean {
    with(event) {  // Make sure the message wasn't created by a bot.
        return message.author?.isBot == false
    }
}
```

```kotlin
suspend fun isNotDM(event: MessageCreateEvent): Boolean {
    with(event) {  // Make sure the message wasn't sent as part of a DM.
        return message.channel.asChannel().type != ChannelType.DM
    }
}
```

These checks can be passed directly to the `check` function in your command 
(if it operates on MessageCreateEvent objects) or event handler.

```kotlin
command {
    check(::isNotBot, ::isNotDM)
}
```

This is already pretty useful, but we could make our checks more wide-reaching.

#### Checks with parameters

In order to write a check which may need to match against another object, we can create a builder function. Let's
look at `notChannelType`, which is a check provided with this framework.

```kotlin
fun notChannelType(channelType: ChannelType): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean {
        with(event) {
            return message.channel.asChannel().type != channelType
        }
    }

    return ::inner
}
```

This function creates a closure around an inner function, and returns that inner function. We can make use of
it like this:

```kotlin
event<MessageCreateEvent> {
    check(notChannelType(ChannelType.DM))
}
```

This allows us to create far more useful checks that can operate across a variety of options.

#### Generic checks

In some cases, you'll want to write checks that can deal with multiple types of event. In order to facilitate
this, we provide a `CheckUtils` module containing a set of functions that can return Kord Behaviors for a given
event. For example, a check written to support events that concern specific members might look like this:

```kotlin
fun hasRole(role: Role): suspend (Event) -> Boolean {
    suspend fun inner(event: Event): Boolean {
        val member = memberFor(event) ?: return false

        return member.asMember().roles.toList().contains(role)
    }

    return ::inner
}
```

We provide the following functions:

* `channelFor(event: Event) -> ChannelBehavior?` 
* `guildFor(event: Event): GuildBehavior?`
* `memberFor(event: Event): MemberBehavior?`
* `messageFor(event: Event): MessageBehavior?`
* `roleFor(event: Event): RoleBehavior?`
* `userFor(event: Event): UserBehavior?`

These functions will return `null` for an unsupported event type. All the checks bundled with this framework
support generic events - if a check receives an unsupported type, that check will simply fail. These failures
will be logged at debug level, so enable SLF4J debug logging if you're not sure - or just use a debugger,
I'm not your mom.

#### Combinators

If you don't want to simply require that all checks pass for a command or event handler, you can use
a combinator check. We currently bundle one such check: `or`.

```kotlin
event<MessageCreateEvent> {
    check(or(  // Check that the channel is either...
        channelType(ChannelType.DM)  // A DM channel,
        channelType(ChannelType.GuildNews)  // Or a news channel.
    ))
}
```

#### Bundled Checks

For a full list of bundled checks, please take a look at 
[the API documentation for the checks package](/kotlindoc/kord-extensions/kord-extensions/com/kotlindiscord/kord/extensions/checks/).
