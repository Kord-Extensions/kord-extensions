---
hide:
- navigation
---

# Getting Started

We recommend making use of Gradle for your build scripts, with a `kordexVersion` entry in your `gradle.properties`. Please
note that Kord (and Kord Extensions) requires **Kotlin 1.4 or later**.

[![Release](https://img.shields.io/nexus/r/com.kotlindiscord.kord.extensions/kord-extensions?nexusVersion=3&logo=gradle&color=blue&label=Release&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-releases:com%2Fkotlindiscord%2Fkord%2Fextensions%2Fkord-extensions) [![Snapshot](https://img.shields.io/nexus/s/com.kotlindiscord.kord.extensions/kord-extensions?logo=gradle&color=orange&label=Snapshot&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-snapshots:com%2Fkotlindiscord%2Fkord%2Fextensions%2Fkord-extensions)

=== "build.gradle.kts"

    ```kotlin
    repositories {
        maven {
            name = "Kotlin Discord"
            url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
        }
    }
    
    dependencies {
        implementation("com.kotlindiscord.kord.extensions:kord-extensions:$kordexVersion")
    }
    ```

=== "build.gradle"

    ```groovy
    repositories {
        maven {
            name = "Kotlin Discord"
            url = "https://maven.kotlindiscord.com/repository/maven-public/"
        }
    }
    
    dependencies {
        implementation "com.kotlindiscord.kord.extensions:kord-extensions:$kordexVersion"
    }
    ```

=== "pom.xml"

    ```xml
    <project>
        <repositories>
            <repository>
                <id>kotlin-discord</id>
                <name>Kotlin Discord</name>
                <url>https://maven.kotlindiscord.com/repository/maven-public/</url>
            </repository>
        </repositories>
        
        <dependencies>
            <dependency>
              <groupId>com.kotlindiscord.kord.extensions</groupId>
              <artifactId>kord-extensions</artifactId>
              <version>VERSION</version>
            </dependency>
        </dependencies>
    </project>
    ```

# A Basic Extension

Kord Extensions is a relatively complex library, but the API it provides is fairly simple. In this example, we'll 
create an extension containing a command, and we'll create a bot with that extension installed on it.

## Initial Files

Create an extension by extending the `Extension` class.

### TestExtension.kt

```kotlin
class TestExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name = "test"  // The registered name for our extension

    class TestArgs : Arguments() {  // The arguments our command takes
        // A single required string argument
        val string by string("string", "String argument")  

        // Multiple boolean arguments, requiring at least one
        val bools by booleanList("bools", "Multiple boolean arguments")  
    }

    // This will be called when the extension gets set up
    override suspend fun setup() {
        command(::Arguments) {  // Define a command
            // The name of the command
            name = "test"

            // A description for the help command to show
            description = "Test command, please ignore"

            action {  // This block will be executed when the command is run
                message.channel.createEmbed {  // Kord: Create an embed
                    title = "Test response"
                    description = "Test description"

                    field {
                        name = "String"
                        value = arguments.string  // Required string is never null
                    }

                    field {
                        name = "Bools (${arguments.bools.size})"
                        value = arguments.bools.joinToString(", ") { "`$it`" }
                    }
                }
            }
        }
    }
}
```

Finally, create your `main` function, creating a bot, adding your extension to it and starting it up.

```kotlin
suspend fun main() {
    // New instance of the bot provided by Kord Extensions
    val bot = ExtensibleBot(env("TOKEN")!!) {  // Discord bot token for logging in, using the env util function
        extensions {
            // Add the extension, the bot will instantiate it
            add(::TestExtension)
        }
    }

    // Start the bot, blocking the current coroutine
    bot.start()  
}
```

## Test it out

Set the `TOKEN` environment variable to a Discord bot token, and run your application. Wait for it to connect, and send `!help test` in a DM or any channel the bot has access to.

<figure>
    <img src="assets/test-command-help.png" width="300" /> 
</figure>

Now, try running the command! For example, `!test String yes no false` will give you:

<figure>
    <img src="assets/test-command-output.png" width="300" /> 
</figure>
