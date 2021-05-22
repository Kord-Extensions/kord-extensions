# Time4J

[![Discord: Click here](https://img.shields.io/static/v1?label=Discord&message=Click%20here&color=7289DA&style=for-the-badge&logo=discord)](https://discord.gg/gjXqqCS)
[![Release](https://img.shields.io/nexus/r/com.kotlindiscord.kord.extensions/time4j?nexusVersion=3&logo=gradle&color=blue&label=Release&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-releases:com%2Fkotlindiscord%2Fkord%2Fextensions%2Ftime4j) [![Snapshot](https://img.shields.io/nexus/s/com.kotlindiscord.kord.extensions/time4j?logo=gradle&color=orange&label=Snapshot&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-snapshots:com%2Fkotlindiscord%2Fkord%2Fextensions%2Ftime4j)

The Time4J module provides converters and parsers that allow you to work with Time4J Durations in your commands.

## Getting Started

* **Maven repo:** `https://maven.kotlindiscord.com/repository/maven-public/`
* **Maven coordinates:** `com.kotlindiscord.kord.extensions:time4j:VERSION`

```kotlin
val kordExVersion: String by project

dependencies {
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:$kordExVersion")
    implementation("com.kotlindiscord.kord.extensions:time4j:$kordExVersion")
}
```

**Note:** Be sure to call `TZDATA.init()` before your bot starts up, or you'll get lots of errors!

## Converters

The following converters are included:

* `T4JDurationCoalescingConverter`
* `T4JDurationConverter`

These can be added to your `Arguments` objects using the following functions:

* `coalescedT4jDuration`
* `defaultingCoalescedT4jDuration`
* `defaultingT4jDuration`
* `t4jDurationList`
* `t4jDuration`
* `optionalCoalescedT4jDuration`
* `optionalT4jDuration`

## Utilities

Function              | Description
:-------------------- | :----------
`Duration .toHuman`   | For a Time4J Duration, return a human-readable string formatted for the given locale
`Duration .toSeconds` | For a Time4J Duration, return the total number of seconds it contains
