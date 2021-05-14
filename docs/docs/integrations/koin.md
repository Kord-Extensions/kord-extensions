# Koin

[Koin](https://insert-koin.io/) is a lightweight service locator framework, written in pure Kotlin. It's a fairly
popular framework that's often used in place of a larger dependency injection framework like Dagger, and Kord Extensions
supports it as a first-class citizen.

??? question "Do I have to use this?"
    Koin integration is entirely optional, but it cannot be unbundled from the main distribution at the moment. The
    integration is still in the early stages, and will be made more useful over time.

    We expect Koin integration to be more useful for users developing their own extensions, but you are of course
    free to make use of it however you like.

??? tip "Koin contexts"
    Koin is designed to be used with a single global context by default. In order to support multiple individual bots in one
    application, however, Kord Extensions creates a separate Koin context for every instance of `ExtensibleBot`. You can
    access it on the `ExtensibleBot#koin` property.

## Registering modules

In order to register a Koin module, call the `koin.loadModules` function before you start your bot.

```kotlin
val config = MyBotConfig()

val bot = ExtensibleBot(config.token) {
    commands {
        prefix = config.prefix
    }
}

suspend fun main() {
    bot.koin.module { single { config } }

    bot.start()
}
```

## Using Koin

All extensions inherit `KoinComponent`, which means that all relevant Koin functions will be present within the 
extension. You can make use of these directly - Kord Extensions makes use of the global Koin instance.

```kotlin
class MyExtension(bot: ExtensibleBot) : KoinExtension(bot) {
    val sentry: SentryAdapter by inject()
}
```

## Bundled modules

The following modules are registered automatically. They do not have any qualifiers (it wouldn't make sense to have
multiple instances registered at once in most cases), but you can supply your own alternatives as necessary by
passing `override = true` to the `module` function when you create your module.

Type                     | Notes
:----------------------- | :----
`ExtensibleBot`          | The current instance of the bot
`ExtensibleBotBuilder`   | The current extensible bot settings object
`Kord`                   | Current Kord instance, **registered after `bot.start()` is called**
`MessageCommandRegistry` | Class that keeps track of message commands, and is in charge of executing them and finding the right prefixes
`SentryAdapter`          | Sentry adapter created for [the Sentry integration](/integrations/sentry)
`SlashCommandRegistry`   | Class that keeps track of slash commands, and is in charge of executing and registering them
`TranslationsProvider`   | Class providing access to [translations](/concepts/i18n)

We'll be updating this list further as parts of Kord Extensions are modularized.
