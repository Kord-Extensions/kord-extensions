# Sentry

[Sentry](https://sentry.io/) is a web-based tool used for keeping track of errors in your applications. Kord Extensions
provides a first-party build-in integration for anyone that uses Sentry to keep track of errors in their production
bots, with full support for breadcrumbs in your commands and event handlers so you can tell Sentry exactly what
went wrong.

??? tip "Discord webhooks?"
    Sentry does not support Discord webhooks out of the box - however, Discord does support Slack-format webhooks. As
    Slack requires current integrations to use OAuth, Sentry has hidden their legacy integration - but it can still be
    used. To find it, head to the settings for your project and append `/plugins/slack/` to the URL - for example,
    `https://sentry.io/settings/my-org/projects/bot/plugins/slack/`. The webhook URL should also have `/slack` appended,
    so it looks like this: `https://discordapp.com/api/webhooks/{ID}/{TOKEN}/slack`

## Setting up

Kord Extensions ships with the Sentry library by default, but no logging integration - we recommend matching the
version of your logging integration with the Sentry library shipped with Kord, but of course you can treat it just
like any other transitive dependency.

The `ExtensibleBot` object contains a `sentry` property referring to a `SentryAdapter` instance - to enable the
Sentry integration, you'll need to set this object up using its `init` function. For example:

```kotlin
val bot = ExtensibleBot("!", TOKEN)

if (System.getenv().getOrDefault("SENTRY_DSN", null) != null) {
    bot.sentry.init {
        dsn = System.getenv("SENTRY_DSN")
        environment = environment = System.getenv().getOrDefault("ENVIRONMENT", "production")
        release = "..."  // However you get your Sentry release info
    }
}
```

There's an alternative version of the `init` function that you can pass parameters to instead, but we highly recommend
the use of the lambda-based function since it directly maps to Sentry's own `init` method.

## Adding context to errors

Sentry's main method of collecting error context is to add breadcrumbs. Breadcrumbs essentially record the steps that
were taken to reach your problem, including extra data that can provide additional context.

Commands and event handlers both have built-in Sentry reporting, and include a mechanism for keeping track of
breadcrumbs that you add as your command or event is processing. You can do this in one of two ways:

1. Use the `breadcrumb` function on the `CommandContext` or `EventContext` object to add a breadcrumb
2. Use the `breadcrumbs` mutable list on the `CommandContext` or `EventContext` object, and use the
   `bot.sentry.createBreadcrumb` function to create breadcrumbs to add to it - this is particularly useful if you need
   to pass the list around to different functions to collect breadcrumbs from them

When an error occurs, the bot will create a Sentry scope configured with information about the command or event that
caused the error, and it'll create an initial breadcrumb corresponding to the start of processing. It'll then take
all the breadcrumbs you created, add them to the scope, and submit the scope directly to Sentry.

There's nothing more that you need to do - you'll see the issue appear in Sentry with all of its breadcrumbs and
associated context!

<figure>
    <a href="/assets/sentry-breadcrumbs.png">
        <img src="/assets/sentry-breadcrumbs.png" width="100%" alt="Sentry breadcrumbs" /> 
    </a>
    <a href="/assets/sentry-data.png">
        <img src="/assets/sentry-data.png" width="100%" alt="Sentry data" /> 
    </a>
</figure>

For a list of supported breadcrumb types, please see
[the Sentry documentation](https://develop.sentry.dev/sdk/event-payloads/breadcrumbs/#breadcrumb-types).

## Collecting user feedback

If an error occurs during command processing, and you have the Sentry extension enabled (which it is by default),
the returned error message will contain a Sentry ID and information on how to submit a piece of user feedback. This
includes a `feedback` command, which will be automatically registered by the Sentry extension.

If you wish to disable this, pass `false` for the `addSentryExtension` constructor param for your `ExtensibleBot`
instance, and both the Sentry ID in the error and the `feedback` command will be disabled.

<a href="/assets/sentry-feedback.png">
    <figure>
        <img src="/assets/sentry-feedback.png" width="100%" alt="Sentry feedback" /> 
    </figure>
</a>

## Manual usage

You can, of course, make use of Sentry [as per its documentation](https://docs.sentry.io/platforms/java/). However,
bear in mind that both Kord and Kord Extensions are coroutine-based, and that means that a lot of the things you do
will be asynchronous.

Because of this, you'll want to collect breadcrumbs and other context yourself instead of creating a scope early -
the best way of doing this is with a simple list of `Breadcrumb` objects. When an error happens, you can create the
Sentry scope, add the breadcrumbs and other context, and submit it all at once. Failure to do this will result in
issues, as Sentry uses a simple stack that it pushes and pops scopes onto without regard for any context - so you
may end up adding breadcrumbs to the wrong scope, or submitting a scope from another part of the application too
early if you don't watch out for this!
