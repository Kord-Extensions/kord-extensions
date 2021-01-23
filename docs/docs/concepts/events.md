# Events

Events represent a single action, either by a user, Discord itself, or your bot. They're one of the primary ways
your bot will react to things happening on Discord, and they're an absolute essential concept to grasp.

??? info "Subclassing these types"
    While the event handler and context types that are about to be described do support subclassing, we do not 
    currently provide a way to provide your subclasses in the typical manner. If you have a use-case for this 
    then please let us know, and we'll prioritise it!

## Event Handlers

In order to react to events, you'll need to add an event handler to your extension. Event handlers represent blocks 
of code that will be run in response to an event, along with some associated metadata. Creating one is simple - just
use the `event` function in your extension's `setup` function.

```kotlin
event<EventClass> {  // this: EventHandler<EventClass>
    check { event -> true }  // Return `false` to prevent the action
    check(::returnTrue)  // You can also pass it a function

    action {  // this: EventContext
        // Code to run when the event is received
    }
}
```

Use the `check` function to define a set of predicates that must all return `true` in order for the event handler to 
be run - you can read more about checks on [the Checks page](/concepts/checks). Once all the checks pass, the `action`
lambda will be called.

Note that the `action` lambda above is a receiver function, where `this`is bound to an `EventContext` object.

## Event Context

An `EventContext` object is a light wrapper around the event type you're working with, and it exists only for the
duration of your event handler's `action`. It exists to provide a little extra context and functionality for your event
handler.

`EventContext` objects expose the following properties, where `T` is the same type as the event being handled.

Name           | Type                       | Description
:------------- | :------------------------: | :----------
`breadcrumbs`  | `MutableList <Breadcrumb>` | List of Sentry breadcrumbs, for the [Sentry intgration](/integrations/sentry)
`eventHandler` | `EventHandler <T>`         | Current event handler instance
`event`        | `T`                        | Current event being handled

Additionally, `EventContext` objects expose the following functions.

Name         | Description
:----------- | :----------
`breadcrumb` | Convenience function to create and add a Sentry breadcrumb, for the [Sentry intgration](/integrations/sentry)

## Event Types

Kord Extensions supports every Kord event, which you can find 
[in Kord's documentation](https://kordlib.github.io/kord/core/core/index.html). Kord events are the primary type
of event that any bot will be making use of, so it's important to explore Kord's events and figure out what you're
going to need.

Additionally, Kord Extensions supports custom events. The following additional event types are provided:

Type                    | Description
:---------------------- | :---------------------------------------------------------------------------------
`ExtensionStateEvent`   | Fired when an extension's loading state changes, containing an `ExtensionState` enum value that may be `FAILED_LOADING`, `FAILED_UNLOADING`, `LOADED`, `LOADING`, `UNLOADED` or `UNLOADING`

## Custom Events

A custom event may essentially be any type, but events are matched on their types - for example, you can't match 
specific strings, only the `String` type. For this reason, the `ExtensionEvent` interface exists - implement it for
your event objects, and use the types you create for matching in your event handlers.

To fire your event, you can use the `send` convenience function on the `ExtensibleBot` object.

```kotlin
class NoteEvent(override val bot: ExtensibleBot, val note: String) : ExtensionEvent

// ...

bot.send(NoteEvent(bot, "Sample note"))
```

There are no restrictions on what your event can contain or what functionality it exposes. If it makes sense to have
a mutable event, feel free!
