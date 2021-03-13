# Common

[![Discord: Click here](https://img.shields.io/static/v1?label=Discord&message=Click%20here&color=7289DA&style=for-the-badge&logo=discord)](https://discord.gg/gjXqqCS)
[![Release](https://img.shields.io/nexus/r/com.kotlindiscord.kordex.ext.common/ext-common?nexusVersion=3&logo=gradle&color=blue&label=Release&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-releases:ext.common%2Fext-common)
[![Snapshot](https://img.shields.io/nexus/s/com.kotlindiscord.kordex.ext.common/ext-common?logo=gradle&color=orange&label=Snapshot&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-snapshots:ext.common%2Fext-common)

The Common module provides shared code that is intended for use by other extensions, and extra extensions that provide
additional services for other extensions to use. While this does make it largely a developer tool and library, users
may also want to configure the bundled extensions.

## Provided Extensions

* `EmojiExtension` - keeps track of Discord custom emoji present on the bot's guilds, providing simple ways for other
  extensions to get at them (or default to something else if the emoji aren't available).

### Getting Started

* **Maven repo:** `https://maven.kotlindiscord.com/repository/maven-public/`
* **Maven coordinates:** `com.kotlindiscord.kordex.ext.common:ext-common:VERSION`

This module doesn't contain much that a user may need to interact with directly, but if you're using another module
that makes use of one of the extensions in this one, you may need to configure them as explained below. As of this
writing, none of the bundled extensions add any commands or user-facing components.

At its simplest, you can add the extensions directly to your bot with no further configuration. For example:

```kotlin
suspend fun main() {
    val bot = ExtensibleBot(System.getenv("TOKEN")) {
        commands {
            defaultPrefix = "!"
        }

        extensions {
            extCommon { }
        }
    }

    bot.start()
}
```

### Configuration: Emoji Extension

* **Env var prefix:** `KORDEX_EMOJI`
* **System property prefix:** `kordex.emoji`

This extension makes use of the Konf library for configuration. Included in the JAR is a default configuration file,
`kordex/emoji/default.toml`. You may configure the extension in one of the following ways:

* **TOML file as a resource:** `kordex/emoji/config.toml`
* **TOML file on the filesystem:** `./config/ext/emoji.toml`
* **Environment variables,** prefixed with `KORDEX_EMOJI_`, upper-casing keys and replacing `.` with `_` in key names
* **System properties,** prefixed with `kordex.emoji.`

For an example, feel free to 
[read the included default.toml](https://github.com/Kord-Extensions/ext-common/blob/root/src/main/resources/kordex/emoji/default.toml). 
The following configuration keys are available:

* `emoji.guilds`: List of guild IDs to index custom emoji from, if required - omit this or set it to an empty list and
  all guilds will be indexed, in the order the bot joined them in.
* `emoji.overrides`: Mapping of emoji names to guild IDs, if you need emoji with a specific name to come from a
  specific guild while ignoring the sorted list of indexed guilds.

If you'd like to provide your own configuration adapter, implement the `EmojiConfig` interface in your own class. You
can then register it in the builder when you set up your bot:

```kotlin
suspend fun main() {
  val bot = ExtensibleBot(System.getenv("TOKEN")) {
    commands {
      defaultPrefix = "!"
    }

    extensions {
      extCommon {
          emojiConfig = CustomEmojiConfig()
      }
    }
  }

  bot.start()
}
```

## Abstract Classes

This extension provides a number of abstract classes that you can use when developing your own extensions.

### TomlConfig

The `TomlConfig` abstract class provides a set of functionality that allows extensions that can be configured to
behave consistently, which makes things easier to understand for users. This extension makes use of the Konf library,
to keep things concise. The class constructor takes the following arguments:

Name             | Type           | Description
:--------------- | :------------: | :----------
`baseName`       | `String`       | Module/extension name in lowerCamelCase
`specs`          | `vararg: Spec` | All of the Konf `Spec` objects that should be loaded, in the order they should be loaded in
`resourcePrefix` | `String`       | Name for the resource group, `"kordex"` (by default) for all KordEx modules
`configFolder`   | `String`       | Name for the inner config folder, `"ext"` (by default) for KordEx extension modules.
`configModifier` | `Config.()`    | Lambda that will be inserted into the `Config` object instantiation, if you need to customize it.

This class takes the following steps to resolve a configuration, with the values from later steps overriding earlier ones:

1. Look for `$resourcePrefix/$baseName/default.toml` in the JAR's resources (you should ship this with your module)
2. Look for `$resourcePrefix/$baseName/config.toml` in the JAR's resources (users can provide this in their dist)
3. Look for `./config/$configFolder/$baseName.toml` on the filesystem (for users to write their own config files)
4. Look for env vars prefixed with `$resourcePrefix_$baseName`, in `ALL_CAPS`
5. Look for system properties prefixed with `$resourcePrefix.$baseName.`

When extending this class, we recommend you create an interface containing getter functions that should return whatever
values are required by your extension, to be implemented by your primary configuration class. This makes it easy to
allow users to specify their own configuration classes for their own specific use-cases - for example, loading the
configuration from a database. Here's an example of one way you can do this:

=== "MyConfigSpec"

    ```kotlin
    object MyConfigSpec : ConfigSpec() {
        val enabled by required<Boolean>()
        val guild by required<Snowflake>()
    }
    ```

=== "MyConfigAdapter"

    ```kotlin
    interface MyConfigAdapter {
        suspend fun isEnabled(): Boolean
        suspend fun getGuild(): Snowflake
    }
    ```

=== "MyTomlConfig"

    ```kotlin
    class MyTomlConfig : MyConfigAdapter, TomlConfig("my", MyConfigSpec) {
        override suspend fun isEnabled(): Boolean = config[MyConfigSpec.enabled]
        override suspend fun getGuild(): Snowflake = config[MyConfigSpec.guild]
    }
    ```

### SerializedData

The `SerializedData` abstract class provides a set of functionality that allows you to serialize (and deserialize)
data (generally, extension state) to disk. This class makes use of `kotlinx.serialization`, and can be customized for 
different formats. The class constructor takes the following arguments:

Name             | Type             | Description
:--------------- | :--------------: | :----------
`T`              | `TypeVar: Any`   | Type of the class you plan to serialize.
`baseName`       | `String`         | Filename, excluding extension.
`dataFolder`     | `String`         | Folder within the `./data` folder to place the file within.
`serializerObj`  | `KSerializer<T>` | Serializer for your data class - pass `serializer()` if you don't have a custom one.
`defaultBuilder` | `() -> T`        | Builder function or lambda that creates a new instance of your data class, with defaults set up. If all of the parameters in your data class have default values, you can pass the constructor using `::T` instead.
`fileExtension`  | `String`         | File extension, defaulting to `"json"` - if you're using a custom format, you can change this in your subclass.

By default, this class assumes you want to use JSON, and will serialize to JSON files on disk. You can change this
behaviour in your subclasses by overriding the `decode` and `encode` functions and changing the `fileExtension`
constructor parameter.

This class saves data in `./data/$dataFolder/$baseName.$fileExtension`. You always need to provide the
`baseName` and `dataFolder` parameters, but `fileExtension` will default to `"json"` if you don't set it. We
recommend providing these as part of the superclass invocation rather than as constructor parameters in your
subclass, if that's appropriate.

If the file doesn't exist or is empty, then it will automatically be filled with the data class created by the
`defaultBuilder` function at load time. Deserialization errors will not result in the file being overwritten with
default data, you'll need to handle that yourself if it's what you need.

!!! warning "Saving Data"
    **Note:** You will need to manually call the `save` and `load` functions. For example, if your data class tracks 
    an `enabled` value, you should implement either a `getEnabled()` and `setEnabled(bool)` function, or a `val` with 
    custom getters and setters - but both options **must** call `save` as appropriate when data is modified!

When extending this class, we recommend you create an interface containing getter and setter functions that should 
interface with the `data` property, to be implemented by your primary data serialization class. This makes it easy to
allow users to specify their own serialization classes for their own specific use-cases - for example, loading the
data from a database, and writing it back. Here's an example of one way you can do this:

=== "MyData"

    ```kotlin
    @Serializable
    data class MyData(
        var enabled: Boolean = true,
        var guild: Snowflake? = null
    )
    ```

=== "MyDataAdapter"

    ```kotlin
    interface MyDataAdapter {
        suspend fun isEnabled(): Boolean
        suspend fun setEnabled(value: Boolean)
    
        suspend fun getGuild(): Snowflake
        suspend fun setGuild(value: Snowflake)
    }
    ```

=== "MyJsonData"

    ```kotlin
    class MyJsonData : MyDataAdapter, SerializedData<MyData>(
        "data", "my", serializer(), ::MyData
    ) {
        init { load() }
    
        override suspend fun isEnabled(): Boolean = data.enabled
        override suspend fun getGuild(): Snowflake = data.guild

        override suspend fun setEnabled(value: Boolean) { 
            data.enabled = value 
    
            save()
        }

        override suspend fun setGuild(value: Snowflake) { 
            data.guild = value
    
            save()
        }
    }
    ```

