package com.kotlindiscord.kord.extensions.builders

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.ClientResources
import dev.kord.core.cache.KordCacheBuilder
import dev.kord.gateway.Intents
import dev.kord.gateway.builder.PresenceBuilder
import org.koin.core.logger.Level

/**
 * Builder class used for configuring and creating an [ExtensibleBot].
 *
 * This is a one-stop-shop for pretty much everything you could possibly need to change to configure your bot, via
 * properties and a bunch of DSL functions.
 */
public open class ExtensibleBotBuilder {
    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val cacheBuilder: CacheBuilder = CacheBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val commandsBuilder: CommandsBuilder = CommandsBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val extensionsBuilder: ExtensionsBuilder = ExtensionsBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public var intentsBuilder: (Intents.IntentsBuilder.() -> Unit)? = null

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val membersBuilder: MembersBuilder = MembersBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public var presenceBuilder: PresenceBuilder.() -> Unit = { status = PresenceStatus.Online }

    /** Logging level Koin should use, defaulting to ERROR. **/
    public var koinLogLevel: Level = Level.ERROR

    /**
     * DSL function used to configure the bot's caching options.
     *
     * @see CacheBuilder
     */
    public fun cache(builder: CacheBuilder.() -> Unit) {
        builder(cacheBuilder)
    }

    /**
     * DSL function used to configure the bot's command options.
     *
     * @see CommandsBuilder
     */
    public fun commands(builder: CommandsBuilder.() -> Unit) {
        builder(commandsBuilder)
    }

    /**
     * DSL function used to configure the bot's extension options, and add extensions.
     *
     * @see ExtensionsBuilder
     */
    public fun extensions(builder: ExtensionsBuilder.() -> Unit) {
        builder(extensionsBuilder)
    }

    /**
     * DSL function used to configure the bot's intents.
     *
     * @see Intents.IntentsBuilder
     */
    public fun intents(builder: Intents.IntentsBuilder.() -> Unit) {
        this.intentsBuilder = builder
    }

    /**
     * DSL function used to configure the bot's member-related options.
     *
     * @see MembersBuilder
     */
    public fun members(builder: MembersBuilder.() -> Unit) {
        builder(membersBuilder)
    }

    /**
     * DSL function used to configure the bot's initial presence.
     *
     * @see PresenceBuilder
     */
    public fun presence(builder: PresenceBuilder.() -> Unit) {
        this.presenceBuilder = builder
    }

    /** @suppress Internal function used to build a bot instance. **/
    public suspend fun build(token: String): ExtensibleBot {
        val bot = ExtensibleBot(this, token)

        bot.setup()

        extensionsBuilder.extensions.forEach { bot.addExtension(it) }

        return bot
    }

    /** Builder used for configuring the bot's caching options. **/
    public class CacheBuilder {
        /**
         * Number of messages to keep in the cache. Defaults to 10,000.
         *
         * To disable automatic configuration of the message cache, set this to `null` or `0`. You can configure the
         * cache yourself using the [kord] function.
         */
        @Suppress("MagicNumber")
        public var cachedMessages: Int? = 10_000

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public var builder: (KordCacheBuilder.(resources: ClientResources) -> Unit) = {
            if (cachedMessages != null && cachedMessages!! > 0) {
                messages(lruCache(cachedMessages!!))
            }
        }

        /** DSL function allowing you to customize Kord's cache. **/
        public fun kord(builder: KordCacheBuilder.(resources: ClientResources) -> Unit) {
            this.builder = {
                if (cachedMessages != null && cachedMessages!! > 0) {
                    messages(lruCache(cachedMessages!!))
                }

                builder.invoke(this, it)
            }
        }
    }

    /** Builder used for configuring the bot's command options. **/
    public class CommandsBuilder {
        /** Whether to invoke commands on bot mentions, in addition to using message prefixes. Defaults to `true`. **/
        public var invokeOnMention: Boolean = true

        /** Prefix to require for command invocations on Discord. Defaults to `"!"`. **/
        public var prefix: String = "!"

        /** Whether to register and process slash commands. Defaults to `false`. **/
        public var slashCommands: Boolean = false

        /** Number of threads to use for command execution. Defaults to twice the number of CPU threads. **/
        public var threads: Int = Runtime.getRuntime().availableProcessors() * 2
    }

    /** Builder used for configuring the bot's extension options, and registering custom extensions. **/
    public class ExtensionsBuilder {
        /** @suppress Internal list that shouldn't be modified by the user directly. **/
        public val extensions: MutableList<(ExtensibleBot) -> Extension> = mutableListOf()

        /** Whether to enable the bundled help extension. Defaults to `true`. **/
        public var help: Boolean = true

        /** Whether to enable the bundled Sentry extension. Defaults to `true`. **/
        public var sentry: Boolean = true

        /** Add a custom extension to the bot via a builder - probably the extension constructor. **/
        public fun add(builder: (ExtensibleBot) -> Extension) {
            extensions.add(builder)
        }
    }

    /** Builder used for configuring the bot's member-related options. **/
    public class MembersBuilder {
        /** @suppress Internal list that shouldn't be modified by the user directly. **/
        public var guildsToFill: MutableList<Snowflake>? = mutableListOf()

        /**
         * Whether to request the presences for the members that are requested from the guilds specified using the
         * functions in this class.
         *
         * Requires the `GUILD_PRESENCES` privileged intent. Make sure you've enabled it for your bot!
         */
        public var fillPresences: Boolean? = null

        /**
         * Add a list of guild IDs to request members for.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        @JvmName("fillSnowflakes")  // These are the same for the JVM
        public fun fill(ids: Collection<Snowflake>): Boolean? =
            guildsToFill?.addAll(ids)

        /**
         * Add a list of guild IDs to request members for.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        @JvmName("fillLongs")  // These are the same for the JVM
        public fun fill(ids: Collection<Long>): Boolean? =
            guildsToFill?.addAll(ids.map { Snowflake(it) })

        /**
         * Add a list of guild IDs to request members for.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        @JvmName("fillStrings")  // These are the same for the JVM
        public fun fill(ids: Collection<String>): Boolean? =
            guildsToFill?.addAll(ids.map { Snowflake(it) })

        /**
         * Add a guild ID to request members for.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        public fun fill(id: Snowflake): Boolean? =
            guildsToFill?.add(id)

        /**
         * Add a guild ID to request members for.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        public fun fill(id: Long): Boolean? =
            guildsToFill?.add(Snowflake(id))

        /**
         * Add a guild ID to request members for.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        public fun fill(id: String): Boolean? =
            guildsToFill?.add(Snowflake(id))

        /**
         * Request members for all guilds the bot is on.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        public fun all() {
            guildsToFill = null
        }

        /**
         * Request no members from guilds at all. This is the default behaviour.
         */
        public fun none() {
            guildsToFill = mutableListOf()
        }
    }
}
