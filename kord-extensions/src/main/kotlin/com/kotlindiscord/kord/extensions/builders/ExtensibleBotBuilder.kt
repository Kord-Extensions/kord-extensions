package com.kotlindiscord.kord.extensions.builders

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.MessageCommandRegistry
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandRegistry
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.cache.api.DataCache
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.ClientResources
import dev.kord.core.Kord
import dev.kord.core.cache.KordCacheBuilder
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
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
    public val messageCommandsBuilder: MessageCommandsBuilder = MessageCommandsBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val slashCommandsBuilder: SlashCommandsBuilder = SlashCommandsBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public open val extensionsBuilder: ExtensionsBuilder = ExtensionsBuilder()

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
     * DSL function used to configure the bot's message command options.
     *
     * @see MessageCommandsBuilder
     */
    public fun messageCommands(builder: MessageCommandsBuilder.() -> Unit) {
        builder(messageCommandsBuilder)
    }

    /**
     * DSL function used to configure the bot's slash command options.
     *
     * @see SlashCommandsBuilder
     */
    public fun slashCommands(builder: SlashCommandsBuilder.() -> Unit) {
        builder(slashCommandsBuilder)
    }

    /**
     * DSL function used to configure the bot's extension options, and add extensions.
     *
     * @see ExtensionsBuilder
     */
    public open fun extensions(builder: ExtensionsBuilder.() -> Unit) {
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
    public open suspend fun build(token: String): ExtensibleBot {
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
         * cache yourself using the [kord] function, and interact with the resulting [DataCache] object using the
         * [transformCache] function.
         */
        @Suppress("MagicNumber")
        public var cachedMessages: Int? = 10_000

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public var builder: (KordCacheBuilder.(resources: ClientResources) -> Unit) = {
            if (cachedMessages != null && cachedMessages!! > 0) {
                messages(lruCache(cachedMessages!!))
            }
        }

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public var dataCacheBuilder: suspend Kord.(cache: DataCache) -> Unit = {}

        /** DSL function allowing you to customize Kord's cache. **/
        public fun kord(builder: KordCacheBuilder.(resources: ClientResources) -> Unit) {
            this.builder = {
                if (cachedMessages != null && cachedMessages!! > 0) {
                    messages(lruCache(cachedMessages!!))
                }

                builder.invoke(this, it)
            }
        }

        /** DSL function allowing you to interact with Kord's [DataCache] before it connects to Discord. **/
        public fun transformCache(builder: suspend Kord.(cache: DataCache) -> Unit) {
            this.dataCacheBuilder = builder
        }
    }

    /** Builder used for configuring the bot's slash command options. **/
    public class SlashCommandsBuilder {
        /** Whether to register and process slash commands. Defaults to `false`. **/
        public var enabled: Boolean = false

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public var slashRegistryBuilder: (ExtensibleBot) -> SlashCommandRegistry = { SlashCommandRegistry(it) }

        public val checkList: MutableList<suspend (InteractionCreateEvent) -> Boolean> = mutableListOf()

        /**
         * Register the builder used to create the [SlashCommandRegistry]. You can change this if you need to make
         * use of a subclass.
         */
        public fun slashRegistry(builder: (ExtensibleBot) -> SlashCommandRegistry) {
            slashRegistryBuilder = builder
        }

        /**
         * Define a check which must pass for a command to be executed. This check will be applied to all
         * slash commands.
         *
         * A command may have multiple checks - all checks must pass for the command to be executed.
         * Checks will be run in the order that they're defined.
         *
         * This function can be used DSL-style with a given body, or it can be passed one or more
         * predefined functions. See the samples for more information.
         *
         * @param checks Checks to apply to all slash commands.
         */
        public fun check(vararg checks: suspend (InteractionCreateEvent) -> Boolean) {
            checks.forEach { checkList.add(it) }
        }

        /**
         * Overloaded check function to allow for DSL syntax.
         *
         * @param check Check to apply to all slash commands.
         */
        public fun check(check: suspend (InteractionCreateEvent) -> Boolean) {
            checkList.add(check)
        }
    }

    /** Builder used for configuring the bot's message command options. **/
    public class MessageCommandsBuilder {
        /** Whether to invoke commands on bot mentions, in addition to using message prefixes. Defaults to `true`. **/
        public var invokeOnMention: Boolean = true

        /** Prefix to require for command invocations on Discord. Defaults to `"!"`. **/
        public var defaultPrefix: String = "!"

        /** Whether to register and process message commands. Defaults to `true`. **/
        public var enabled: Boolean = true

        /** Number of threads to use for command execution. Defaults to twice the number of CPU threads. **/
        public var threads: Int = Runtime.getRuntime().availableProcessors() * 2

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public var prefixCallback: suspend (MessageCreateEvent).(String) -> String = { defaultPrefix }

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public var messageRegistryBuilder: (ExtensibleBot) -> MessageCommandRegistry = { MessageCommandRegistry(it) }

        public val checkList: MutableList<suspend (MessageCreateEvent) -> Boolean> = mutableListOf()

        /**
         * Register a lambda that takes a [MessageCreateEvent] object and the default prefix, and returns the
         * command prefix to be made use of for that message event.
         *
         * This is intended to allow for different message command prefixes in different contexts - for example,
         * guild-specific prefixes.
         */
        public fun prefix(builder: suspend (MessageCreateEvent).(String) -> String) {
            prefixCallback = builder
        }

        /**
         * Register the builder used to create the [MessageCommandRegistry]. You can change this if you need to make
         * use of a subclass.
         */
        public fun messageRegistry(builder: (ExtensibleBot) -> MessageCommandRegistry) {
            messageRegistryBuilder = builder
        }

        /**
         * Define a check which must pass for the command to be executed. This check will be applied to all commands.
         *
         * A command may have multiple checks - all checks must pass for the command to be executed.
         * Checks will be run in the order that they're defined.
         *
         * This function can be used DSL-style with a given body, or it can be passed one or more
         * predefined functions. See the samples for more information.
         *
         * @param checks Checks to apply to all commands.
         */
        public fun check(vararg checks: suspend (MessageCreateEvent) -> Boolean) {
            checks.forEach { checkList.add(it) }
        }

        /**
         * Overloaded check function to allow for DSL syntax.
         *
         * @param check Checks to apply to all commands.
         */
        public fun check(check: suspend (MessageCreateEvent) -> Boolean) {
            checkList.add(check)
        }
    }

    /** Builder used for configuring the bot's extension options, and registering custom extensions. **/
    public open class ExtensionsBuilder {
        /** @suppress Internal list that shouldn't be modified by the user directly. **/
        public open val extensions: MutableList<(ExtensibleBot) -> Extension> = mutableListOf()

        /** Whether to enable the bundled help extension. Defaults to `true`. **/
        public var help: Boolean = true

        /** Whether to enable the bundled Sentry extension. Defaults to `true`. **/
        public var sentry: Boolean = true

        /** Add a custom extension to the bot via a builder - probably the extension constructor. **/
        public open fun add(builder: (ExtensibleBot) -> Extension) {
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
