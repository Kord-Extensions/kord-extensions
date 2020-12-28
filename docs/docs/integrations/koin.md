# Koin

[Kotlin](https://insert-koin.io/) is a lightweight dependency injection framework, written in pure Kotlin. It's a
fairly popular DI framework, and Kord Extensions supports it as a first-class citizen.

??? question "Do I have to use this?"
    Koin integration is entirely optional, but it cannot be unbundled from the main distribution at the moment. The
    integration is still in the early stages, and will be made more useful over time.

    We expect Koin integration to be more useful for users developing their own extensions, but you are of course
    free to make use of it however you like.

??? tip "Koin contexts"
    Koin is designed to be used with a single global context by default. In order to support multiple individual bots
    in one application, however, Kord Extensions creates a separate Koin context for every instance of `ExtensibleBot`.
    You can access it on the `ExtensibleBot#koin` property.

## Registering modules

In order to register a Koin module, call the `koin.declare` function, before you start your bot.

```kotlin
val config = MyBotConfig()
val bot = ExtensibleBot(config.prefix, config.token)

suspend fun main() {
    bot.koin.declare(config)
    
    bot.start()
}
```

## Using Koin in extensions

All extensions provide a `koin` property, and a set of convenience functions that map directly to Koin's functions.
To avoid name conflicts, these are prefixed with a `k`.

* `kInject` -> `koin.inject`
* `kInjectOrNull` -> `koin.injectOrNull`
* `kGet` -> `koin.get`
* `kGetOrNull` -> `koin.getOrNull`

These functions will always make use of the current `ExtensibleBot`'s `koin` property.

## Bundled modules

The following modules are registered automatically.


Name            | Qualifier  | Notes
:-------------- | :--------: | :----
`ExtensibleBot` | `"bot"`    | The current instance of the bot
`Kord`          | `"kord"`   | Current Kord instance, **registered after `bot.start()` is called**
`SentryAdapter` | `"sentry"` | Sentry adapter created for [the Sentry integration](/integrations/sentry.md)
