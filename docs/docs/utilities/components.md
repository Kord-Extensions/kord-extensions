# Components

To provide an integrated API for working with Discord's buttons (and other components going forward), Kord Extensions
provides a simple DSL builder for adding components to your messages. This should be sufficient for most use-cases,
but you can always extend the `Components` class or write your own component handling if you need to.

## Components DSL

To get started, create a message or follow-up and invoke the `components` DSL function.

```kotlin
ephemeralFollowUp {
    content = "Buttons!"
    
    components(60) {  // A timeout of 60 seconds, optional
        interactiveButton {
            label = "Button one!"
            // Style defaults to `Primary`

            action { // Easy button actions
                respond("Button one pressed!")
            }
        }

        interactiveButton {
            label = "Button two!"
            style = ButtonStyle.Secondary

            action {
                respond("Button two pressed!")
            }
        }

        disabledButton {
            // Labels are optional if you have an emoji
            emoji("❎")  // Easily add an emoji from various sources
        }

        linkButton {
            label = "Google"  // You can provide both a label and emoji if you like
            emoji("🔗")

            url = "https://google.com"
        }
    }
}
```

This will automatically sort all the components you've provided into rows (provided you have 25 or less), send them to
Discord, and set up event handlers to call their actions as required.

??? note "Sorting behaviour and empty rows"
    All messages may have up to 5 rows of components, and each row may contain up to 5 components. For buttons that
    don't have a row provided, the `Components` class will attempt to pack the components into each row that has space,
    in order. If you provide two components on the first row and three on the second, unsorted components will try to
    fill in the empty space in those rows before moving onto the latter rows.

    Components are always sorted from the first row to the last. If you want to avoid automatic sorting, then simply
    provide a specific row for each of your components. Empty rows are also skipped when adding components to your
    message, so you can provide five components on the last row and allow the `Components` class to sort everything
    else above them.

The `components` DSL function will return the `Components` object that contains all the components you just defined,
and handles their actions. If you didn't set a timeout or would like to trigger it early, you can call the`stop()` 
function, and your bot will stop listening for component actions.

### Common Functionality

All component functions take the following parameters, in addition to the DSL function body:

Parameter | Type   | Description
:-------- | :----- | :----------
`row`     | `Int?` | Optionally, provide a row (from 1-4) to append the component to that row directly.

#### Button Properties

The following properties are common to all button types. As usual, properties marked with :warning: are required, but 
may not be if you provide another property.

Parameter      | Type                    | Description
:------------- | :---------------------- | :----------
`label`        | `String?`               | :warning: The button's label - required if no emoji was provided.
`partialEmoji` | `DiscordPartial Emoji?` | :warning: The button's emoji - required if no label was provided. You'll probably want to use one of the `emoji` functions instead of setting this directly.

#### Button Functions

The following functions are common to all button types.

Function  | Description
:-------- | :----------
`emoji`   | A convenience function that will set the button's `partialEmoji` property based on a given Unicode emoji, guild custom emoji or reaction emoji.

### Disabled Buttons

Disabled buttons don't do anything when clicked. Their builders have the following properties, with required properties
marked with :warning::

Parameter | Type           | Description
:-------- | :------------- | :----------
`id`      | `String`       | The button's unique ID on Discord, which defaults to a randomly-generated UUID. Normally this would be used for click actions, but disabled buttons don't have one!
`style`   | `Button Style` | The button's style on Discord, which defaults to `Primary`.

### Interactive Buttons

Interactive buttons have a click action that's handled by the bot. Their builders have the following properties, with
required properties marked with :warning::

Parameter      | Type            | Description
:------------- | :-------------- | :----------
`ackType`      | `AutoAck Type?` | If the button isn't being sent as part of a slash command interaction, then this will be used for the automatic acknowledgement - just like it would be for slash commands. Defaults to `EPHEMERAL` if you're working within a slash command, and `PUBLIC` if you're working with normal messages. Disable with `null`.
`deferredAck`  | `Boolean`       | Set this to `true` to send a deferred acknowledgement instead of a normal one, which will clear the "processing" state of the button interaction. This is `false` by default, which will wait for you to send a `followUp` before clearing the "processing" state.
`followParent` | `Boolean`       | By default, button interactions that happen as part of a slash command follow the ack type of that slash command's context. If you don't want that, then set this to `false`.
`id`           | `String`        | The button's unique ID on Discord, which defaults to a randomly-generated UUID. Normally this would be used for click actions, but disabled buttons don't have one!
`style`        | `Button Style`  | The button's style on Discord, which defaults to `Primary`.

Additionally, the following functions are available, with functions that must be called marked with :warning::

Function  | Description
:-------- | :----------
`action`  | :warning: Set the action to be taken when this button is clicked. All interactive button actions are treated as receiver functions to an `InteractiveButtonContext` object.
`check`   | Provide a check lambda or callable which must pass for the button action to be run. If a check fails, the interaction will be acknowledged with a deferred ack - meaning the "processing" status will be cleared - and the `action` will not be called.

??? note "Interactive button contexts"
    The `InteractiveButtonContext` class provides the execution context for your button's click actions. It largely
    provides the same properties and functions that the `SlashCommandContext` object does, with the addition of
    `components` and `interaction`, which represent the relevant `Components` container and properly-typed
    `ComponentInteraction` object respectively.

### Link Buttons

Interactive buttons direct the user to a URL when clicked. Their builders have the following properties, with required 
properties marked with :warning::

Parameter | Type           | Description
:-------- | :------------- | :----------
`id`      | `String`       | The button's unique ID on Discord, which defaults to a randomly-generated UUID. Normally this would be used for click actions, but disabled buttons don't have one!
`style`   | `Button Style` | The button's style on Discord, which defaults to `Primary`.
`url`     | `String`       | :warning: The URL to direct the user to.

## Tracking buttons between restarts

While the `Components` object doesn't provide any specific tracking for component actions after the bot has been 
restarted, you can implement this yourself with relative ease.

1. Each of the component functions (eg, `interactiveButton()`) return the builder object used. You can store the ID of
   the component used here for later.
2. When the bot restarts, you can load your stored IDs and create new `Components` objects for the components for each
   message.
3. Store the action for each UUID into the `Components` object using the relevant map - for example, 
   `interactiveActions` for interactive button actions.
4. Finally, call the `startListening` function with the desired timeout, and your component actions should be functional
   again.
