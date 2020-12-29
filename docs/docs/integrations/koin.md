# Koin

[Koin](https://insert-koin.io/) is a lightweight service locator framework, written in pure Kotlin. It's a
fairly popular framework that's often used in place of a larger dependency injection framework like Dagger, and 
Kord Extensions supports it as a first-class citizen.

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

In order to register a Koin module, call the `koin.loadModules` function before you start your bot.

```kotlin
val config = MyBotConfig()
val bot = ExtensibleBot(
    prefix = config.prefix, 
    token = config.token
)

suspend fun main() {
    bot.koin.loadModules(
        listOf(
            module {
                single { config }
            }
        )
    )
    
    bot.start()
}
```

## Using Koin

If you'd like to make use of Koin in your extensions, you can extend `KoinExtension` instead of `Extension`. This
class is functionally the same as `Extension`, but it implements the `KoinComponent` interface via the included
`KoinAccessor` class. This means that all relevant Koin functions will be present within the extension, but they
will delegate to the `koin` property on your `ExtensibleBot` instead of a global Koin context.

```kotlin
class MyExtension(bot: ExtensibleBot) : KoinExtension(bot) {
    val sentry: SentryAdapter by inject()
}
```

If you need to make use of Koin in your other classes, you can extend `KoinAccessor` using composition. You can
always extend the class directly as well, but composition is useful when your class already extends another class.

```kotlin
class MyClass(
    bot: ExtensibleBot,
    koinAccessor: KoinComponent = KoinAccessor(bot)
): KoinComponent by koinAccessor {
    val sentry: SentryAdapter by inject()
}
```

## Bundled modules

The following modules are registered automatically.

Name            | Qualifier  | Notes
:-------------- | :--------: | :----
`ExtensibleBot` | `"bot"`    | The current instance of the bot
`Kord`          | `"kord"`   | Current Kord instance, **registered after `bot.start()` is called**
`SentryAdapter` | `"sentry"` | Sentry adapter created for [the Sentry integration](/integrations/sentry)

We'll be updating this list further as parts of Kord Extensions are modularized.
