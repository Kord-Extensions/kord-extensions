### Commands

A command is an action invoked by a user on Discord. They're the primary method of interacting
with most bots, and require a lot of consideration. We've attempted to make things as easy
and friendly to work with as possible.

You can define commands using the DSL functions available in [extensions](./extension).

#### Defining Commands

Define your commands in your extension's `setup` function. Here's a basic example:

```kotlin
override suspend fun setup() {
    command { // this: Command
        name = "ping"

        action { // this: CommandContext
            message.channel.createMessage("Pong!")
        }
    }
}
```

#### Basic Options

All commands require a `name` and an `action`, defined similarly to the above example.

* The `name` of your command should be unique across all installed extensions, and is
  the name that is used to invoke the command on Discord.
* The `action` is the body of the command, which will be executed when a user invokes
  the command.

In addition to these required options, command support a further set of optional
options:

* `aliases: Array<String>` - This is a list of alternative names that can be used to
  invoke the command. Note that if a command exists with a `name` that exists as an
  alias in another command, the command with the matching `name` takes priority.
  
  You can have as many aliases as you like.
* `description: String = """` - This is a description for your command, which will 
  be displayed by the `help` command. Try to make it descriptive, while also keeping 
  it short!
* `enabled: Boolean = true` - If you disable a command by setting this to `false`, it 
  cannot be invoked and will not be shown in the `help` command. This can be modified 
  at runtime, if required.
* `hidden: Boolean = false` - If you want it to be possible to invoke your command, 
  but would still prefer it to be hidden from the `help` command, then you can set this
  to `true`.
* `signature: String = ""` - The command's signature represents how the arguments to the
  command should be formatted. For the sake of consistency, all optional arguments should
  be specified like this: `[name]`. Required arguments should use `<name>`. Lists can
  be either required or optional, but should contain an ellipsis - `<name ...>`.
  
  The signature will be shown in help commands, and may also be automatically generated from a
  data class. See the below section on command arguments for more information on signature
  generation.

#### Checks

You will likely want to write commands that are only invoked based on a set of conditions.
In order to facilitate this in a manner that allows you to easily reuse these conditions,
commands support a [checks system](check).

```kotlin
suspend fun defaultCheck(event: MessageCreateEvent): Boolean {
    with(event) {
        return when {
            message.author?.id == bot.kord.getSelf().id -> false  // Check that we didn't send this message.
            message.author!!.isBot == true              -> false  // Check that another bot didn't send this message.
            else                                        -> true
        }
    }
}

// ...

command {
    name = "ping"

    check(::defaultCheck)

    action {
        message.channel.createMessage("Pong!")
    }
}
```

If you don't need to make use of the check elsewhere, you can also specify them using a DSL syntax.

```kotlin
command {
    name = "ping"

    check {
        it.message.author?.id != bot.kord.getSelf().id &&  // Check that we didn't send this message.
        it.message.author?.isBot == false  // Check that another bot didn't send this message.
    }

    action {
        message.channel.createMessage("Pong!")
    }
}
```

You can have as many checks as you need - simply call `check` again for every check you wish to add. If
you have multiple check functions to add, you may also specify them all as arguments for a single `check` call.

```kotlin
check(
    ::defaultCheck,  // Default checks we do for every command
    notChannelType(ChannelType.DM)  // Ensure the command isn't being invoked in a DM
)
```

#### Command Arguments

In order to make life as easy as possible, you have the option of creating a data class that represents the
arguments for your command, allowing for automatic generation of the command signature and parsing of command
arguments. For example:

```kotlin
data class SampleArgs(
    val optionalInt: Int = 1,  // An optional integer.
    val requiredUsers: List<User>  // A list of User objects.
)

command {
    name = "sample"

    signature<SampleArgs>()  // Automatically generate the command signature.

    action {
        val parsed = parse<SampleArgs>()  // Automatically parse the command arguments into the data class

        with(parsed) {  // this: SampleArgs
            message.channel.createMessage(
                "Arguments: optionalInt = $optionalInt | " +
                    "requiredUsers = ${requiredUsers.joinToString(", ") { it.username }}"
            )
        }
    }
}
```

In order to facilitate this manner of parsing, the parser follows these rules:

* First, split the message into arguments. Spaces separate arguments from each other, but users may surround
  a set of arguments with double quotes in order to have them treated as a single argument, preserving the spaces.
* For each parameter in the data class' primary constructor (not the properties defined in the body, if any): 
  Take an argument and try to convert it to the correct type.
    * If the parameter is a List, attempt to convert all remaining arguments for the command.
    * If the argument could not be converted, but you gave the parameter a default value, skip it and try the next 
      parameter with the same argument.
    * If the argument could not be converted and does not have a default value, the user will be presented with an
      error message.
* Once all the parameters have been converted, the data class is constructed using them. This is then returned
  from the `parse` function.

The following types are supported by the parser:

* Basic types: `String`, `Regex`, `Boolean`, `Int`, `Short`, `Long`, `Float`, `Double`, `BigDecimal`, `BigInteger`.
* Kord types: `Channel`, `Guild`, `GuildEmoji`, `Role`, `User`.

**Note:** `GuildEmoji` and `Role` objects will be retrieved from the within the current guild. It is not possible
to specify which guild to use at this time, but we may add support for that later.
