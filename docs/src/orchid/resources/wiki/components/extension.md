### Extensions

An `Extension` represents a single unit of functionality, encompassing
related commands and event handlers, which can be easily defined using
the DSL provided.

All extensions need to subclass the `Extension` class. They'll also
need to override two things:

* The `setup` suspended function. This function is where all
  commands and event handlers should be registered.
* The `name` string. This is a unique identifier for the extension,
  and extension objects can be retrieved by name if needed.

You may register commands and event handlers using the `command` 
and `event` DSL functions respectively.

```kotlin
class TestExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "test"

    override suspend fun setup() {
        // Register your commands and event handlers here.
    }
}
```

The `setup` function will be called exactly once, when the first 
`ReadyEvent` is received after connecting to Discord. This ensures
that the `setup` function is able to retrieve objects from Discord.
