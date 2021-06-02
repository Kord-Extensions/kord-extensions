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
hooks                     | DSL function |         | Register lambdas that run at various stages in the bot's lifecycle
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
check                     | DSL Function |            | Register a [check](/concepets/checks) that will be applied to all slash commands.
defaultGuild              | DSL Function | `null`     | For testing, specify a guild ID to use for all slash commands that would normally be global. You can still override this by using the `guild()` function provided when defining your slash commands.
enabled                   | Boolean      | `false`    | Whether to support registration and invocation of slash commands.
slashRegistry             | DSL Function |            | If you'd like to use a `SlashCommandRegistry` subclass, then you can register a builder that returns it here - most likely the constructor.

### Extensions configuration

For more information on what extensions are and how they work, please
[see the Extensions concept page](/concepts/extensions).

```kotlin
val bot = ExtensibleBot(token) {
    extensions {
        sentry = false

        add(::LogsExtension)
        add(::TestExtension)
    }
}
```

Name                      | Type         | Default    | Description
:------------------------ | :----------: | :--------: | :----------
add                       | Function     |            | Use this function to add your own custom extensions to the bot
help                      | DSL Function |            | Used to configure the bundled help extension
sentry                    | Boolean      | `true`     | Whether to enabled the bundled [Sentry extension](/integrations/sentry)

External modules that add extensions are free to add extension functions to this class, which gives users a convenient
way to configure them. If you're using an external module, we recommend reading the documentation for any modules you
may be using.

#### Help Extension


```kotlin
val bot = ExtensibleBot(token) {
    extensions {
        help {
            colour { DISCORD_GREEN }
            
            deletePaginatorOnTimeout = true
            deleteInvocationOnPaginatorTimeout = true
        }
    }
}
```

Name                               | Type         | Default    | Description
:--------------------------------- | :----------: | :--------: | :----------
check                              | DSL Function |            | Register a [check](/concepets/checks) that will be applied to all help extension commands
colour                             | DSL Function |            | Register a callback returning a Kord `Color` object to use it for the help command pages - this is called for every page generated, so feel free to return randomized colours if you'd like to
deleteInvocationOnPaginatorTimeout | Boolean      | `false`    | Whether to try to delete the user's `!help` command when the paginator times out
deletePaginatorOnTimeout           | Boolean      | `false`    | Whether to delete the help command's output paginator when it times out
enableBundledExtension             | Boolean      | `true`     | Whether to enable the bundled help extension
paginatorTimeout                   | Long         | `60_000`   | How long to wait until a paginator times out and becomes unusable - defaults to 60 seconds

### Hooks

Hooks allow you to set up lambdas that are to be run at various points in the bot's lifecycle, allowing you to set up
the bot precisely how you need it.

All hook functions may be called multiple times, if you need to add multiple hooks for the same lifecycle stage.

```kotlin
val bot = ExtensibleBot(token) {
    hooks {
        created {
            println("ExtensibleBot object created, but not yet set up.")
        }
    }
}
```

??? important "ExtensibleBotBuilder subclasses"
    Please note that some hooks are called by the `build` function - if you override this function, you need to remember
    to similarly call the corresponding hook functions yourself.

Property         | Default | Description
:--------------- | :-----: | :--------
kordShutdownHook | `true`  | Whether Kord should register a hook that logs out of the gateway as part of the application shutdown process

Lifecycle Function        | Description
:------------------------ | :----------
afterExtensionsAdded      | Lambdas registered here are called after all the extensions specified in the `extensions` builder above have been registered
afterKoinSetup            | Lambdas registered here are called just after Koin has been set up - you can register overriding modules here via `loadModule {}`
beforeExtensionsAdded     | Lambdas registered here are called before all the extensions specified in the `extensions` builder above have been registered
beforeKoinSetup           | Lambdas registered here are called just before Koin is set up, right after the `startKoin` call - you can register modules early here via `loadModule {}`
beforeStart               | Lambdas registered here are called just before the bot tries to connect to Discord
created                   | Lambdas registered here are called just after the `ExtensibleBot` object has been created, before it's been set up
extensionAdded            | Lambdas registered here are called every time an extension is added successfully, with the extension object as a parameter
setup                     | Lambdas registered here are called after the `ExtensibleBot` object has been created and set up

### I18n configuration

The i18n builder allows you to configure the default locale, and register locale resolvers and a translations 
provider. The default locale is `SupportedLocales.ENGLISH`, if you don't change it.

```kotlin
val bot = ExtensibleBot(token) {
    i18n {
        defaultLocale = SupportedLocales.ENGLISH
    }
}
```

Property                  | Type                 | Default                      | Description
:------------------------ | :------------------: | :--------------------------: | :----------
defaultLocale             | Locale               | `ENGLISH`                    | The default locale to use, when your locale resolvers don't return a different one
translationsProvider      | TranslationsProvider | `ResourceBundleTranslations` | Implementation of `TranslationsProvider` responsible for transforming translation keys into strings, replacing placeholders as necessary

Function             | Description
:------------------- | :----------
localeResolver       | Call this to register a lambda (or callable) that takes a `Guild?`, `Channel` and `User?` argument and returns a `Locale` object, or `null` to move on to the next resolver - you can use this to set up, for example, guild-specific locales
translationsProvider | Call this to register a builder (usually a constructor) returning a TranslationsProvider instance - this is called immediately, you can't conditionally dispatch to multiple providers with this

For more information on i18n and translations, see [the i18n page](/concepts/i18n).

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

A few properties are available to you, for getting access to a few internals.

??? info "Further properties"
    There are other non-private properties available, but they aren't necessarily something you'll need to touch. Most
    of the properties are `open` to facilitate niche use-cases that require extending the ExtensibleBot class.

This object used to have many other useful properties here, but to keep things clean those properties have been moved
and should now be accessed via Koin. For more information, see [the Koin integration page](/integrations/koin).

Name            | Type                      | Description
:-------------- | :-----------------------: | :----------
`eventHandlers` | `List <EventHandler>`     | All currently-registered event handler objects
`extensions`    | `Map <String, Extension>` | All currently-loaded extension objects

## Functions

A number of functions are available to you as well.

??? info "Further functions"
    There are other non-private functions available, but they aren't necessarily something you'll need to touch. Most
    of the functions are `open` to facilitate niche use-cases that require extending the ExtensibleBot class, but a
    handful are `inline` for the sake of avoiding function call overhead or making use of reified types.

Name              | Description
:---------------- | :----------
`findExtension`   | Find the first loaded extension matching the given type, or `null` if there isn't one
`findExtensions`  | Get a list of all loaded extensions matching the given type
`loadExtension`   | Set up a previously-unloaded extension
`start`           | Connect to Discord, blocking the coroutine scope
`send`            | Send an event to all event handlers - either a Kord `Event` or an `ExtensionEvent` object
`unloadExtension` | Tear down a previously-loaded extension
