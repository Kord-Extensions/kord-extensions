# Java Time

[![Discord: Click here](https://img.shields.io/static/v1?label=Discord&message=Click%20here&color=7289DA&style=for-the-badge&logo=discord)](https://discord.gg/gjXqqCS)
[![Release](https://img.shields.io/nexus/r/com.kotlindiscord.kord.extensions/java-time?nexusVersion=3&logo=gradle&color=blue&label=Release&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-releases:com%2Fkotlindiscord%2Fkord%2Fextensions%2Fjava-time) [![Snapshot](https://img.shields.io/nexus/s/com.kotlindiscord.kord.extensions/java-time?logo=gradle&color=orange&label=Snapshot&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-snapshots:com%2Fkotlindiscord%2Fkord%2Fextensions%2Fjava-time)

The Java Time module provides converters and parsers that allow you to work with Java 8 Durations in your commands.
This used to be the standard Duration converter, but was split out into a module as some users didn't need it.

## Getting Started

* **Maven repo:** `https://maven.kotlindiscord.com/repository/maven-public/`
* **Maven coordinates:** `com.kotlindiscord.kord.extensions:java-time:VERSION`

```kotlin
val kordExVersion: String by project

dependencies {
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:$kordExVersion")
    implementation("com.kotlindiscord.kord.extensions:java-time:$kordExVersion")
}
```

## Converters

The following converters are included:

* `J8DurationCoalescingConverter`
* `J8DurationConverter`

These can be added to your `Arguments` objects using the following functions:

* `coalescedJ8Duration`
* `defaultingCoalescedJ8Duration`
* `defaultingJ8Duration`
* `j8DurationList`
* `j8Duration`
* `optionalCoalescedJ8Duration`
* `optionalJ8Duration`

## Utilities

Function             | Description
:------------------- | :----------
`Duration .toHuman`  | For a Java Time Duration, return a human-readable string in the form `a days, b hours, c minutes and d seconds` (omitting zero-values)
