# The Bot Object

The bot object is the core of your application. All bots that make use of Kord Extensions must make use of the
ExtensibleBot class - usually by creating an instance of it and using it as a jumping-off point.

??? faq "Extending the ExtensibleBot class"
    Generally speaking, it's best to use ExtensibleBot directly and approach us if there's some functionality missing 
    you need, but the option to extend it is always there if you have a very niche use-case.
    
    If you're not sure whether your use-case is appropriate for a wider public release, please open an issue - we're
    always happy to have a chat with you!

## Creating a Bot

The first thing you'll want to do is create an instance of ExtensibleBot. A basic setup looks something like this:

```kotlin
val token = "..."  // Get your bot token
val prefix = "!"  // Prefix required before all command invocations

val bot = ExtensibleBot(token, prefix)  // Create the bot
```

The ExtensibleBot constructor takes a number of arguments. We recommend supplying them using keyword arguments, just
in case the order needs to be changed later on.

??? bug "Invoke command on mention"
    Currently a bug exists that prevents the functionality referenced by `invokeCommandOnMention` from working. We'll
    look into this as soon as we can - but if you need this functionality for your bots, contact us and we'll
    prioritise it.

Name   |   Type   |   Default   | Description
:----- | :------: | :---------: | :------------
`token` | `String` | | The Discord bot token to login with
`prefix` | `String` | | The prefix required before all command invocations
`addHelpExtension` | `Boolean` | `true` | Whether to add the bundled help extension automatically
`addSentryExtension` | `Boolean` | `true` | Whether to add the bundled [Sentry integration](/integrations/sentry) extension automatically
`invokeCommandOnMention` | `Boolean` | `true` | Whether commands may also be invoked by mentioning the bot
`messageCacheSize` | `Int` | `10_000` | How many messages to keep in the messages cache by default
`commandThreads` | `Int` | CPUs * 2 | How many threads to use for the command execution threadpool
`guildsToFill` | `List <Snowflake>` | `[ ]` | A list of guilds to request all members for during the connection phase. This requires the `GuildMembers` intent, specified in the `start` function
`fillPresences` | `Boolean?` | `null` | Whether to request presences for the above members (`true`/`false`, or `null` for the default). This requires the `GuildPresences` intent, specified in the `start` function

## Adding extensions

When you've written some [Extensions](/concepts/extensions), you'll need to add them to the bot before they can be
used. This can be done at any point, but we recommend doing it before you connect to Discord, as many extensions rely
on Discord's `ReadyEvent` to set up.

```kotlin
bot.addExtension(LogsExtension::class)
bot.addExtension(TestExtension::class)
```

## Starting up

When you're all set up, and you've registered all your extensions, you can call the `start` function to create a Kord
instance and connect to Discord.

```kotlin
bot.start()
```

This function takes an optional lambda, which you can use to provide an intents builder or presence builder via the 
`presence` and `intents` DSL functions.

??? missing "Not Implemented: Sharding"
    Sharding is currently not supported. We haven't had a chance to look at it yet, as none of the developers are
    currently working on bots that require sharding.

    If you need sharding, please contact us and we can work out the details.

Name       |   Type               | Description
:--------- | :------------------: | :----------
`intents`  | `IntentsBuilder.()`  | DSL function that allows you to provide a set of intents to use when logging into Discord - for more information, see [the Discord documentation on intents](https://discordpy.readthedocs.io/en/latest/intents.html)
`presence` | `PresenceBuilder.()` | DSL function that allows you to specify a presence builder, used to set the bot's status and activity on Discord

```kotlin
bot.start {
    intents { +Intents.all }

    presence {  // This is the default behaviour if you don't provide your own presence builder
        status = PresenceStatus.Online
    }
}
```

## Properties

A few properties are available to you, for getting access to Kord or querying some of the bot's state.

??? info "Further properties"
    There are other non-private properties available, but they aren't necessarily something you'll need to touch. Most
    of the properties are `open` to facilitate niche use-cases that require extending the ExtensibleBot class.

Name | Type | Description
:--- | :--: | :----------
`kord` | `Kord` | Current connected Kord instance, if the bot has been started
`commands` | `List <Command>` | All currently-registered command objects
`eventHandlers` | `List <EventHandler>` | All currently-registered event handler objects
`extensions` | `Map <String, Extension>` | All currently-loaded extension objects

## Functions

A number of functions are available to you as well.

??? info "Further functions"
    There are other non-private functions available, but they aren't necessarily something you'll need to touch. Most
    of the functions are `open` to facilitate niche use-cases that require extending the ExtensibleBot class, but a
    handful are `inline` for the sake of avoiding function call overhead.

Name | Description
:--- | :----------
`start` | Connect to Discord, blocking the coroutine scope
`send` | Send an event to all event handlers - either a Kord `Event` or an `ExtensionEvent` object 
`addExtension` | Instantiate and load an extension 
`loadExtension` | Set up a previously-unloaded extension
`unloadExtension` | Tear down a previously-loaded extension
