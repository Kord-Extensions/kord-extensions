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
val commandPrefix = "!"  // Prefix required before all command invocations - "!" is the default and can be omitted

val bot = ExtensibleBot(token) {
    messageCommands {
        defaultPrefix = commandPrefix
    }
}
```
The ExtensibleBot class is configured using a builder pattern, providing a set of properties and DSL functions
that you can make use of to configure your bot.

Name                      | Type         | Default | Description
:------------------------ | :----------: | :-----: | :----------
koinLogLevel              | Level        | `ERROR` | Koin logger level
cache                     | DSL function |         | Configure the bot's caching options using a builder
commands                  | DSL function |         | Configure the bot's command options using a builder
extensions                | DSL function |         | Configure the bot's extension options using a builder, and add custom extensions
intents                   | DSL function |         | Configure the bot's intents using a builder
members                   | DSL function |         | Configure the bot's member-related options using a builder
presence                  | DSL function |         | Configure the bot's initial presence using a builder

Each of the DSL functions configures a specific part of the bot, and they're documented in their own sections below.

### Cache configuration

```kotlin
val bot = ExtensibleBot(token) {
    cache {
        cachedMessages = null

        kord {  // https://github.com/kordlib/kord/wiki/Caching
            messages(MySpecialCache())
        }
    }
}
```

Name                      | Type         | Default  | Description
:------------------------ | :----------: | :------: | :----------
cachedMessages            | Int?         | `10_000` | Number of messages to store in Kord's cache by default - set this to `null` to disable, or if you're customizing Kord's message cache yourself using the `kord` DSL function
kord                      | DSL function |          | Customize Kord's cache yourself using a builder - for more information, [see Kord's wiki on caching](https://github.com/kordlib/kord/wiki/Caching)
transformCache            | DSL function |          | Interact with Kord's `DataCache` object before it connects to Discord - for more information, [see Kord's wiki on caching](https://github.com/kordlib/kord/wiki/Caching)

### Command configuration

Kord Extensions supports two types of commands - message commands and slash commands. They are configured separately.

#### Message commands

```kotlin
val bot = ExtensibleBot(token) {
    messageCommands {
        defaultPrefix = "?"

        invokeOnMention = true
    }
}
```

Name                      | Type         | Default    | Description
:------------------------ | :----------: | :--------: | :----------
check                     | DSL function |            | Register a [check](/concepets/checks) that will be applied to all message commands.
defaultPrefix             | String       | `!`        | Prefix required before all command invocations
enabled                   | Boolean      | `true`     | Whether to support registration and invocation of message commands
invokeOnMention           | Boolean      | `true`     | Whether commands may also be invoked by mentioning the bot
messageRegistry           | DSL Functon  |            | If you'd like to use a `MessageCommandRegistry` subclass, then you can register a builder that returns it here.
prefix                    | DSL Function |            | Register a receiver function for `MessageEvent` objects, that takes the configured `defaultPrefix` and returns a `String` that should be used as the message command prefix, given the context. This can be used to, for example, set up unique command prefixes for different guilds. **Note:** this is used in several places, and not just the command invocation logic - so make sure you don't interact with Discord any more than you have to!
threads                   | Int          | `CPus * 2` | How many threads to use for the command execution threadpool

#### Slash commands

```kotlin
val bot = ExtensibleBot(token) {
    slashCommands {
        enabled = true
    }
}
```

Name                      | Type         | Default    | Description
:------------------------ | :----------: | :--------: | :----------
check                     | DSL function |            | Register a [check](/concepets/checks) that will be applied to all message commands.
enabled                   | Boolean      | `false`    | Whether to support registration and invocation of slash commands
slashRegistry             | DSL Functon  |            | If you'd like to use a `SlashCommandRegistry` subclass, then you can register a builder that returns it here.
threads                   | Int          | `CPus * 2` | How many threads to use for the command execution threadpool

### Extensions configuration

For more information on what extensions are and how they work, please
[see the Extensions concept page](/concepts/extensions).

```kotlin
val bot = ExtensibleBot(token) {
    extensions {
        help = true
        sentry = false

        add(::LogsExtension)
        add(::TestExtension)
    }
}
```

Name                      | Type         | Default    | Description
:------------------------ | :----------: | :--------: | :----------
add                       | Function     |            | Use this function to add your own custom extensions to the bot
help                      | Boolean      | `true`     | Whether to enabled the bundled help extension
sentry                    | Boolean      | `true`     | Whether to enabled the bundled [Sentry extension](/integrations/sentry)

### Intent configuration

This matches Kord's intents API. For a list of available intents, 
[see the Kord documentation](https://kordlib.github.io/kord/gateway/gateway/dev.kord.gateway/-intent/index.html).

```kotlin
val bot = ExtensibleBot(token) {
    intents {
        +Intents.all
    }
}
```

### Member-related configuration

This builder configures automatic cache filling for members that are part of the guilds the bot is present on. Please
note that you will need the `GUILD_MEMBERS` privileged intent to fill the cache with guild member information, and the
`GUILD_PRESENCES` privileged intent if you'd also like to fill the cache with (and receive) their presences.

```kotlin
val bot = ExtensibleBot(token) {
    members {
        fillPresences = true
        
        all()
    }
}
```

Name                      | Type         | Description
:------------------------ | :----------: | :----------
all                       | Function     | State that you'd like to fill the cache with member info for all guilds the bot is part of
fill                      | Function     | Use this function to specify guild IDs (or collections of guild IDs) that you'd like to cache member information for
fillPresences             | Boolean?     | Set this to `true` to state that you'd like to receive user presences
none                      | Function     | State that you would not like to cache any member info - this is the default behaviour

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
`koinLogLevel` | `Level` | `ERROR` | The default logging level that Koin should use
`handleSlashCommands` | `Boolean` | `false` | Whether to support registration and invocation of slash commands. Setting this to fault will not raise errors for extensions that register slash commands, however - they just won't work

### Presence configuration

This matches Kord's initial presence API. For more information,
[see the Kord documentation](https://kordlib.github.io/kord/gateway/gateway/dev.kord.gateway.builder/-presence-builder/index.html).

```kotlin
val bot = ExtensibleBot(token) {
    presence {
        status = PresenceStatus.Online

        playing("!help for command help")
    }
}
```


## Starting up

When you're all set up, and you've registered all your extensions, you can call the `start` function to create a Kord
instance and connect to Discord.

```kotlin
bot.start()
```

??? missing "Not Implemented: Sharding"
    Sharding is currently not supported. We haven't had a chance to look at it yet, as none of the developers are
    currently working on bots that require sharding.

    If you need sharding, please contact us and we can work out the details.

## Properties

A few properties are available to you, for getting access to Kord, querying some of the bot's state, and some other 
things.

??? info "Further properties"
    There are other non-private properties available, but they aren't necessarily something you'll need to touch. Most
    of the properties are `open` to facilitate niche use-cases that require extending the ExtensibleBot class.

Name            | Type                      | Description
:-------------- | :-----------------------: | :----------
`commands`      | `List <Command>`          | All currently-registered command objects
`eventHandlers` | `List <EventHandler>`     | All currently-registered event handler objects
`extensions`    | `Map <String, Extension>` | All currently-loaded extension objects
`koin`          | `Koin`                    | Koin instance to be made use of instead of the global one
`kord`          | `Kord`                    | Current connected Kord instance, if the bot has been started
`slashCommands` | `SlashCommandRegistry`    | Slash command registry that keeps track of slash commands, and invokes them when the relevant events are received

## Functions

A number of functions are available to you as well.

??? info "Further functions"
    There are other non-private functions available, but they aren't necessarily something you'll need to touch. Most
    of the functions are `open` to facilitate niche use-cases that require extending the ExtensibleBot class, but a
    handful are `inline` for the sake of avoiding function call overhead.

Name              | Description
:---------------- | :----------
`findExtension`   | Find the first loaded extension matching the given type, or `null` if there isn't one
`findExtensions`  | Get a list of all loaded extensions matching the given type
`loadExtension`   | Set up a previously-unloaded extension
`start`           | Connect to Discord, blocking the coroutine scope
`send`            | Send an event to all event handlers - either a Kord `Event` or an `ExtensionEvent` object
`unloadExtension` | Tear down a previously-loaded extension
