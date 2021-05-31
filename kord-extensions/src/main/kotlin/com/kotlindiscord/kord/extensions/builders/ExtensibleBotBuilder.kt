package com.kotlindiscord.kord.extensions.builders

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.annotations.BotBuilderDSL
import com.kotlindiscord.kord.extensions.commands.MessageCommandRegistry
import com.kotlindiscord.kord.extensions.commands.cooldowns.Cooldown
import com.kotlindiscord.kord.extensions.commands.cooldowns.CooldownType
import com.kotlindiscord.kord.extensions.commands.cooldowns.impl.ChannelCooldown
import com.kotlindiscord.kord.extensions.commands.cooldowns.impl.CooldownImpl
import com.kotlindiscord.kord.extensions.commands.cooldowns.impl.GuildCooldown
import com.kotlindiscord.kord.extensions.commands.cooldowns.impl.UserCooldown
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandRegistry
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.i18n.ResourceBundleTranslations
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.kord.cache.api.DataCache
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.ClientResources
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.cache.KordCacheBuilder
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.gateway.Intents
import dev.kord.gateway.builder.PresenceBuilder
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.bind
import org.koin.environmentProperties
import org.koin.fileProperties
import org.koin.logger.slf4jLogger
import java.io.File
import java.util.*
import kotlin.time.*

internal typealias LocaleResolver = suspend (
    guild: GuildBehavior?,
    channel: ChannelBehavior?,
    user: UserBehavior?
) -> Locale?

/**
 * Builder class used for configuring and creating an [ExtensibleBot].
 *
 * This is a one-stop-shop for pretty much everything you could possibly need to change to configure your bot, via
 * properties and a bunch of DSL functions.
 */
@BotBuilderDSL
public open class ExtensibleBotBuilder {
    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val cacheBuilder: CacheBuilder = CacheBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public open val extensionsBuilder: ExtensionsBuilder = ExtensionsBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val hooksBuilder: HooksBuilder = HooksBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val i18nBuilder: I18nBuilder = I18nBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public var intentsBuilder: (Intents.IntentsBuilder.() -> Unit)? = null

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val membersBuilder: MembersBuilder = MembersBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val messageCommandsBuilder: MessageCommandsBuilder = MessageCommandsBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public var presenceBuilder: PresenceBuilder.() -> Unit = { status = PresenceStatus.Online }

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val slashCommandsBuilder: SlashCommandsBuilder = SlashCommandsBuilder()

    /** Logging level Koin should use, defaulting to ERROR. **/
    public var koinLogLevel: Level = Level.ERROR

    /**
     * DSL function used to configure the bot's caching options.
     *
     * @see CacheBuilder
     */
    @BotBuilderDSL
    public fun cache(builder: CacheBuilder.() -> Unit) {
        builder(cacheBuilder)
    }

    /**
     * DSL function used to insert code at various points in the bot's lifecycle.
     *
     * @see HooksBuilder
     */
    @BotBuilderDSL
    public fun hooks(builder: HooksBuilder.() -> Unit) {
        builder(hooksBuilder)
    }

    /**
     * DSL function used to configure the bot's message command options.
     *
     * @see MessageCommandsBuilder
     */
    @BotBuilderDSL
    public fun messageCommands(builder: MessageCommandsBuilder.() -> Unit) {
        builder(messageCommandsBuilder)
    }

    /**
     * DSL function used to configure the bot's slash command options.
     *
     * @see SlashCommandsBuilder
     */
    @BotBuilderDSL
    public fun slashCommands(builder: SlashCommandsBuilder.() -> Unit) {
        builder(slashCommandsBuilder)
    }

    /**
     * DSL function used to configure the bot's extension options, and add extensions.
     *
     * @see ExtensionsBuilder
     */
    @BotBuilderDSL
    public open fun extensions(builder: ExtensionsBuilder.() -> Unit) {
        builder(extensionsBuilder)
    }

    /**
     * DSL function used to configure the bot's intents.
     *
     * @see Intents.IntentsBuilder
     */
    @BotBuilderDSL
    public fun intents(builder: Intents.IntentsBuilder.() -> Unit) {
        this.intentsBuilder = builder
    }

    /**
     * DSL function used to configure the bot's i18n settings.
     *
     * @see I18nBuilder
     */
    @BotBuilderDSL
    public fun i18n(builder: I18nBuilder.() -> Unit) {
        builder(i18nBuilder)
    }

    /**
     * DSL function used to configure the bot's member-related options.
     *
     * @see MembersBuilder
     */
    @BotBuilderDSL
    public fun members(builder: MembersBuilder.() -> Unit) {
        builder(membersBuilder)
    }

    /**
     * DSL function used to configure the bot's initial presence.
     *
     * @see PresenceBuilder
     */
    @BotBuilderDSL
    public fun presence(builder: PresenceBuilder.() -> Unit) {
        this.presenceBuilder = builder
    }

    /** @suppress Internal function used to initially set up Koin. **/
    public open fun setupKoin() {
        startKoin {
            slf4jLogger(koinLogLevel)
            environmentProperties()

            if (File("koin.properties").exists()) {
                fileProperties("koin.properties")
            }

            modules()
        }

        hooksBuilder.runBeforeKoinSetup()

        loadModule { single { this@ExtensibleBotBuilder } bind ExtensibleBotBuilder::class }
        loadModule { single { i18nBuilder.translationsProvider } bind TranslationsProvider::class }
        loadModule { single { messageCommandsBuilder.messageRegistryBuilder() } bind MessageCommandRegistry::class }
        loadModule { single { SentryAdapter() } bind SentryAdapter::class }
        loadModule { single { slashCommandsBuilder.slashRegistryBuilder() } bind SlashCommandRegistry::class }

        hooksBuilder.runAfterKoinSetup()
    }

    /** @suppress Internal function used to build a bot instance. **/
    public open suspend fun build(token: String): ExtensibleBot {
        setupKoin()

        val bot = ExtensibleBot(this, token)

        loadModule { single { bot } bind ExtensibleBot::class }

        hooksBuilder.runCreated(bot)
        bot.setup()
        hooksBuilder.runSetup(bot)

        hooksBuilder.runBeforeExtensionsAdded(bot)
        extensionsBuilder.extensions.forEach { bot.addExtension(it) }
        hooksBuilder.runAfterExtensionsAdded(bot)

        return bot
    }

    /** Builder used for configuring the bot's cooldowns options. **/
    public class CooldownsBuilder {

        /** @suppress **/
        public var implementation: () -> Cooldown = { CooldownImpl() }

        /** @suppress **/
        public var priority: () -> List<CooldownType> = {
            listOf(
                UserCooldown(),
                ChannelCooldown(),
                GuildCooldown()
            )
        }

        /**
         * Defines whether we should automatically clear expired cooldowns in the background.
         */
        public var autoClearCooldowns: Boolean = true

        /**
         * Specifies how often we should clear expired cooldowns.
         */
        @OptIn(ExperimentalTime::class)
        @Suppress("MagicNumber")
        public var autoClearTime: Duration = 5.minutes

        /**
         * Sets the implementation to use for the command's cooldown object.
         */
        public fun implementation(builder: () -> Cooldown) {
            this.implementation = builder
        }

        /**
         * Defines the priority for which cooldowns to check and set.
         */
        public fun priority(builder: () -> List<CooldownType>) {
            this.priority = builder
        }
    }

    /** Builder used for configuring the bot's caching options. **/
    @BotBuilderDSL
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

    /** Builder used for configuring the bot's extension options, and registering custom extensions. **/
    @BotBuilderDSL
    public open class ExtensionsBuilder {
        /** @suppress Internal list that shouldn't be modified by the user directly. **/
        public open val extensions: MutableList<() -> Extension> = mutableListOf()

        /** Whether to enable the bundled help extension. Defaults to `true`. **/
        public var help: Boolean = true

        /** Whether to enable the bundled Sentry extension. Defaults to `true`. **/
        public var sentry: Boolean = true

        /** Add a custom extension to the bot via a builder - probably the extension constructor. **/
        public open fun add(builder: () -> Extension) {
            extensions.add(builder)
        }
    }

    /** Builder used to insert code at various points in the bot's lifecycle. **/
    @Suppress("TooGenericExceptionCaught")
    // We need to catch literally everything in here
    @BotBuilderDSL
    public class HooksBuilder {
        // region: Hook lists

        /**
         * Whether Kord's shutdown hook should be registered. When enabled, Kord logs out of the gateway on shutdown.
         */
        public var kordShutdownHook: Boolean = true

        /** @suppress Internal list of hooks. **/
        public val afterExtensionsAddedList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val afterKoinSetupList: MutableList<() -> Unit> = mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val beforeKoinSetupList: MutableList<() -> Unit> = mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val beforeExtensionsAddedList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val beforeStartList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val createdList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val extensionAddedList: MutableList<suspend ExtensibleBot.(extension: Extension) -> Unit> =
            mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val setupList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

        // endregion

        // region DSL functions

        /**
         * Register a lambda to be called after all the extensions in the [ExtensionsBuilder] have been added. This
         * will be called regardless of how many were successfully set up.
         */
        public fun afterExtensionsAdded(body: suspend ExtensibleBot.() -> Unit): Boolean =
            afterExtensionsAddedList.add(body)

        /**
         * Register a lambda to be called after Koin has been set up. You can use this to register overriding modules
         * via `loadModule` before the modules are actually accessed.
         */
        public fun afterKoinSetup(body: () -> Unit): Boolean =
            beforeKoinSetupList.add(body)

        /**
         * Register a lambda to be called before Koin has been set up. You can use this to register Koin modules
         * early, if needed.
         */
        public fun beforeKoinSetup(body: () -> Unit): Boolean =
            beforeKoinSetupList.add(body)

        /**
         * Register a lambda to be called before all the extensions in the [ExtensionsBuilder] have been added.
         */
        public fun beforeExtensionsAdded(body: suspend ExtensibleBot.() -> Unit): Boolean =
            beforeExtensionsAddedList.add(body)

        /**
         * Register a lambda to be called just before the bot tries to connect to Discord.
         */
        public fun beforeStart(body: suspend ExtensibleBot.() -> Unit): Boolean =
            beforeStartList.add(body)

        /**
         * Register a lambda to be called right after the [ExtensibleBot] object has been created, before it gets set
         * up.
         */
        public fun created(body: suspend ExtensibleBot.() -> Unit): Boolean =
            createdList.add(body)

        /**
         * Register a lambda to be called before after any extension is successfully added to the bot..
         */
        public fun extensionAdded(body: suspend ExtensibleBot.(extension: Extension) -> Unit): Boolean =
            extensionAddedList.add(body)

        /**
         * Register a lambda to be called after the [ExtensibleBot] object has been created and set up.
         */
        public fun setup(body: suspend ExtensibleBot.() -> Unit): Boolean =
            setupList.add(body)

        // endregion

        // region Hook execution functions

        /** @suppress Internal hook execution function. **/
        public suspend fun runAfterExtensionsAdded(bot: ExtensibleBot): Unit =
            afterExtensionsAddedList.forEach {
                try {
                    it.invoke(bot)
                } catch (t: Throwable) {
                    bot.logger.error(t) {
                        "Failed to run extensionAdded hook $it"
                    }
                }
            }

        /** @suppress Internal hook execution function. **/
        public fun runAfterKoinSetup() {
            val logger: KLogger = KotlinLogging.logger {}

            afterKoinSetupList.forEach {
                try {
                    it.invoke()
                } catch (t: Throwable) {
                    logger.error(t) {
                        "Failed to run beforeKoinSetup hook $it"
                    }
                }
            }
        }

        /** @suppress Internal hook execution function. **/
        public fun runBeforeKoinSetup() {
            val logger: KLogger = KotlinLogging.logger {}

            beforeKoinSetupList.forEach {
                try {
                    it.invoke()
                } catch (t: Throwable) {
                    logger.error(t) {
                        "Failed to run beforeKoinSetup hook $it"
                    }
                }
            }
        }

        /** @suppress Internal hook execution function. **/
        public suspend fun runBeforeExtensionsAdded(bot: ExtensibleBot): Unit =
            beforeExtensionsAddedList.forEach {
                try {
                    it.invoke(bot)
                } catch (t: Throwable) {
                    bot.logger.error(t) {
                        "Failed to run beforeExtensionsAdded hook $it"
                    }
                }
            }

        /** @suppress Internal hook execution function. **/
        public suspend fun runBeforeStart(bot: ExtensibleBot): Unit =
            beforeStartList.forEach {
                try {
                    it.invoke(bot)
                } catch (t: Throwable) {
                    bot.logger.error(t) {
                        "Failed to run beforeStart hook $it"
                    }
                }
            }

        /** @suppress Internal hook execution function. **/
        public suspend fun runCreated(bot: ExtensibleBot): Unit =
            createdList.forEach {
                try {
                    it.invoke(bot)
                } catch (t: Throwable) {
                    bot.logger.error(t) {
                        "Failed to run created hook $it"
                    }
                }
            }

        /** @suppress Internal hook execution function. **/
        public suspend fun runExtensionAdded(bot: ExtensibleBot, extension: Extension): Unit =
            extensionAddedList.forEach {
                try {
                    it.invoke(bot, extension)
                } catch (t: Throwable) {
                    bot.logger.error(t) {
                        "Failed to run extensionAdded hook $it"
                    }
                }
            }

        /** @suppress Internal hook execution function. **/
        public suspend fun runSetup(bot: ExtensibleBot): Unit =
            setupList.forEach {
                try {
                    it.invoke(bot)
                } catch (t: Throwable) {
                    bot.logger.error(t) {
                        "Failed to run setup hook $it"
                    }
                }
            }

        // endregion
    }

    /** Builder used to configure i18n options. **/
    @BotBuilderDSL
    public class I18nBuilder {
        /** Locale that should be used by default. **/
        public var defaultLocale: Locale = SupportedLocales.ENGLISH

        /**
         * Callables used to resolve a Locale object for the given guild, channel and user.
         *
         * Resolves to [defaultLocale] by default.
         */
        public var localeResolvers: MutableList<LocaleResolver> = mutableListOf()

        /** Object responsible for retrieving translations. Users should get this via Koin or other methods. **/
        internal var translationsProvider: TranslationsProvider = ResourceBundleTranslations { defaultLocale }

        /** Call this with a builder (usually the class constructor) to set the translations provider. **/
        public fun translationsProvider(builder: (() -> Locale) -> TranslationsProvider) {
            translationsProvider = builder { defaultLocale }
        }

        /** Register a locale resolver, returning the required [Locale] object or `null`. **/
        public fun localeResolver(body: LocaleResolver) {
            localeResolvers.add(body)
        }
    }

    /** Builder used for configuring the bot's member-related options. **/
    @BotBuilderDSL
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

    /** Builder used for configuring the bot's message command options. **/
    @BotBuilderDSL
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
        public var messageRegistryBuilder: () -> MessageCommandRegistry = { MessageCommandRegistry() }

        /**
         * List of command checks.
         *
         * These checks will be checked against all commands.
         */
        public val checkList: MutableList<suspend (MessageCreateEvent) -> Boolean> = mutableListOf()

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public val cooldownsBuilder: CooldownsBuilder = CooldownsBuilder()

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
        public fun messageRegistry(builder: () -> MessageCommandRegistry) {
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

        /**
         * Allows for configuring cooldown settings for commands.
         */
        public fun cooldowns(builder: CooldownsBuilder.() -> Unit) {
            cooldownsBuilder.apply(builder)
        }
    }

    /** Builder used for configuring the bot's slash command options. **/
    @BotBuilderDSL
    public class SlashCommandsBuilder {
        /** Whether to register and process slash commands. Defaults to `false`. **/
        public var enabled: Boolean = false

        /** The guild ID to use for all global slash commands. Intended for testing. **/
        public var defaultGuild: Snowflake? = null

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public var slashRegistryBuilder: () -> SlashCommandRegistry = { SlashCommandRegistry() }

        /**
         * List of slash command checks.
         *
         * These checks will be checked against all slash commands.
         */
        public val checkList: MutableList<suspend (InteractionCreateEvent) -> Boolean> = mutableListOf()

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public val cooldownsBuilder: CooldownsBuilder = CooldownsBuilder()
        
        /** Set a guild ID to use for all global slash commands. Intended for testing. **/
        public fun defaultGuild(id: Snowflake) {
            defaultGuild = id
        }

        /** Set a guild ID to use for all global slash commands. Intended for testing. **/
        public fun defaultGuild(id: Long) {
            defaultGuild = Snowflake(id)
        }

        /** Set a guild ID to use for all global slash commands. Intended for testing. **/
        public fun defaultGuild(id: String) {
            defaultGuild = Snowflake(id)
        }

        /**
         * Register the builder used to create the [SlashCommandRegistry]. You can change this if you need to make
         * use of a subclass.
         */
        public fun slashRegistry(builder: () -> SlashCommandRegistry) {
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

        /**
         * Allows for configuring cooldown settings for slash commands.
         */
        public fun cooldowns(builder: CooldownsBuilder.() -> Unit) {
            cooldownsBuilder.apply(builder)
        }
    }
}
