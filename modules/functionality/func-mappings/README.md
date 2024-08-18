# Mappings Extension

[![Discord: Click here](https://img.shields.io/static/v1?label=Discord&message=Click%20here&color=7289DA&style=for-the-badge&logo=discord)](https://discord.gg/ZKRetPNtvY)

This module contains an extension written to provide Minecraft mappings information on Discord. It makes heavy use
of [linkie-core](https://github.com/shedaniel/linkie-core), which (as of this writing) supports Legacy Yarn, MCP,
Mojang, Plasma, Yarn and Yarrn mappings.

If you're looking for older versions (and older tags), you can find them
[in the archived kordex-modules repository](https://github.com/Kotlin-Discord/kordex-modules/releases) and
[the archived ext-mappings repository](https://github.com/Kord-Extensions/ext-mappings/releases).

# Setting Up

## Gradle Plugin

With the [KordEx Gradle plugin](https://docs.kordex.dev/kordex-plugin.html) applied, add the module to your project:

```kt
kordEx {
	module("func-mappings")
}
```

## Manual Setup

* **Maven repo:** https://snapshots-repo.kordex.dev
* **Maven coordinates:** `dev.kordex.modules:func-mappings:VERSION`

To manually add the module to your project, follow these steps:

1. Add the required Maven repositories to your project:
    * **FabricMC**: `https://maven.fabricmc.net`
    * **QuiltMC (Releases)**: `https://maven.quiltmc.org/repository/release`
    * **QuiltMC (Snapshots)**: `https://maven.quiltmc.org/repository/snapshot`
    * **Shedaniel**: `https://maven.shedaniel.me`
    * **JitPack**: `https://jitpack.io`

2. Add the module to your project.
    - `dev.kordex.modules:func-mappings:VERSION`

# Usage

At its simplest, you can add this extension directly to your bot with no further configuration. For example:

```kotlin
suspend fun main() {
    val bot = ExtensibleBot(System.getenv("TOKEN")) {
        chatCommands {
            enabled = true
        }

        extensions {
            extMappings { }
        }
    }

    bot.start()
}
```

This will install the extension using its default configuration, which enables all mappings namespaces and does not
restrict the commands in any manner. Additional options are available in the `extMappings` builder for you to use -
they're detailed below.

# Usage

This extension provides a number of commands for use on Discord.

* Commands for retrieving information about mappings namespaces: `hashed`, `legacy-yarn`, `mcp`, `mojang`, `plasma`
  , `yarn` and `yarrn`
* Hashed Mojang-specific lookup commands: `hc`, `hf` and `hm`
* Legacy Yarn-specific lookup commands: `lyc`, `lyf` and `lym`
* MCP-specific lookup commands: `mcpc`, `mcpf` and `mcpm`
* Mojang-specific lookup commands: `mmc`, `mmf` and `mmm`
* Plasma lookup commands: `pc`, `pf` and `pm`
* Yarn-specific lookup commands: `yc`, `yf` and `ym`
* Yarrn-specific lookup commands: `yrc`, `yrf` and `yrm`

# Configuration

* **Env var prefix:** `KORDEX_MAPPINGS`
* **System property prefix:** `kordex.mappings`

This extension makes use of the Konf library for configuration. Included in the JAR is a default configuration file,
`kordex/mappings/default.toml`. You may configure the extension in one of the following ways:

* **TOML file as a resource:** `kordex/mappings/config.toml`
* **TOML file on the filesystem:** `config/ext/mappings.toml`
* **Environment variables,** prefixed with `KORDEX_MAPPINGS_`, upper-casing keys and replacing `.` with `_` in key names
* **System properties,** prefixed with `kordex.mappings.`

For an example, feel free to [read the included default.toml](src/main/resources/kordex/mappings/default.toml). The
following configuration keys are available:

* `categories.allowed`: List of categories mappings commands may be run within. When set, mappings commands may not be
  run in other categories, or in guild channels that aren't in categories. This setting takes priority over
  `categories.banned`.
* `categories.banned`: List of categories mappings commands may **not** be run within. When set, mappings commands may
  not be run within the given categories.
* `channels.allowed`: List of channels mappings commands may be run within. When set, mappings commands may not be run
  in other guild channels. This setting takes priority over `channels.banned`.
* `channels.banned`: List of channels mappings commands may **not** be run within. When set, mappings commands may not
  be run within the given channels.
* `guilds.allowed`: List of guilds mappings commands may be run within. When set, mappings commands may not be run in
  other guilds. This setting takes priority over `guilds.banned`.
* `guilds.banned`: List of guilds mappings commands may **not** be run within. When set, mappings commands may not be
  run within the given guilds.
* `settings.namespaces`: List of enabled namespaces. Currently, `hashed-mojang`, `legacy-yarn`, `mcp`, `mojang`
  , `plasma`, `yarn`
  and `yarrn` are supported, and they will all be enabled by default.
* `settings.timeout`: Time (in seconds) to wait before destroying mappings paginators, defaulting to 5 minutes (300
  seconds). Be careful when setting this value to something high - a busy bot may end up running out of memory if
  paginators aren't destroyed quickly enough. This setting only accepts whole numbers.
* `yarn.channels`: List of extra Yarn channels to enable. Currently, only `PATCHWORK` is supported, and it will be
  enabled by default.

**Please note:** Mappings commands will always function when sent to the bot via a private message. However, only the
configured namespaces will be available - the user will not be able to query disabled namespaces.

# Customisation

This extension supports two primary methods of customization: Replacing the config adapter, and registering custom
checks. While the options do require some programming, they should help you to customize its behaviour to suit your
bot's needs.

## Custom Checks

[As described in the Kord Extensions documentation](https://kord-extensions.docs.kotlindiscord.com/concepts/checks/),
Kord Extensions provides a system of checks that can be applied to commands and other event handlers. Checks essentially
allow you to prevent execution of a command depending on the context it was executed within.

This extension allows you to register custom checks by calling the `commandCheck()` and `namespaceCheck()` functions in
the builder, as follows:

```kotlin
val gdudeSnowflake = Snowflake("109040264529608704")
val yarrnChannelId = Snowflake("...")

suspend fun main() {
    val bot = ExtensibleBot(System.getenv("TOKEN")) {
        commands {
            defaultPrefix = "!"
        }

        extensions {
            extMappings {
                commandCheck { command ->  // This is the command name
                    { event ->
                        if (command == "yarn") { // Only limit usage of the `yarn` command
                            event.message.author?.id != gdudeSnowflake  // We don't want gdude using this
                        } else {
                            true
                        }
                    }
                }

                namespaceCheck { namespace ->  // This is the Linkie namespace in use
                    { event ->
                        // If it's not a Yarrn command, or it is a Yarrn command and we're in the Yarrn channel, it's OK
                        namespace != YarrnNamespace || event.channel.id == yarrnChannelId
                    }
                }
            }
        }
    }

    bot.start()
}
```

You can also write this using functions instead of lambdas, of course.

```kotlin
val gdudeSnowflake = Snowflake("109040264529608704")

suspend fun mappingsCheck(namespace: Namespace): suspend (MessageCreateEvent) -> Boolean {
    suspend fun inner(event: MessageCreateEvent): Boolean =
        if (namespace == YarnNamespace) {  // Only limit usage of the `yarn` commands
            event.message.author?.id != gdudeSnowflake  // We don't want gdude using this
        } else {
            true
        }

    return ::inner
}

suspend fun main() {
    val bot = ExtensibleBot(System.getenv("TOKEN")) {
        commands {
            defaultPrefix = "!"
        }

        extensions {
            extMappings {
                namespaceCheck(::mappingsCheck)
            }
        }
    }

    bot.start()
}
```

The approach you take is up to you!

## Replacing the Config Adapter

If you need some other form of configuration - for example, from a database - you can implement the
`MappingsConfigAdapater` interface in your own classes and store an instance of it in the `config` variable in the
builder. While going into detail on each function is a little out of scope for this document, you can find more
information in the following places:

* [MappingsConfigAdapter interface](./src/main/kotlin/com/kotlindiscord/kord/extensions/modules/extra/mappings/configuration/MappingsConfigAdapter.kt)
* [TomlMappingsConfig class](./src/main/kotlin/com/kotlindiscord/kord/extensions/modules/extra/mappings/configuration/TomlMappingsConfig.kt)

```kotlin
suspend fun main() {
    val bot = ExtensibleBot(System.getenv("TOKEN")) {
        commands {
            defaultPrefix = "!"
        }

        extensions {
            extMappings {
                config = CustomMappingsConfig()
            }
        }
    }

    bot.start()
}
```

# Licensing & Attribution

This extension makes use of [linkie-core](https://github.com/shedaniel/linkie-core), and contains code adapted
from [linkie-discord](https://github.com/shedaniel/linkie-discord). Both projects are licensed under the Apache License
2.0, which you can find in [LICENSE-linkie.md](./LICENSE-linkie.md), distributed within the
`ext-mappings` JAR, on [the linkie-core GitHub](https://github.com/shedaniel/linkie-core/blob/master/LICENSE.md) or
on [the linkie-discord GitHub](https://github.com/shedaniel/linkie-discord/blob/master/LICENSE.md). Both
`linkie-core` and `linkie-discord` are property of [shedaniel](https://github.com/shedaniel).
