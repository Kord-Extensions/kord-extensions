/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(PrivilegedIntent::class)

package dev.kordex.core.builders

import dev.kord.cache.api.DataCache
import dev.kord.common.Color
import dev.kord.common.asJavaLocale
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.common.kLocale
import dev.kord.core.ClientResources
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.builder.kord.KordBuilder
import dev.kord.core.cache.KordCacheBuilder
import dev.kord.core.cache.lruCache
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.Interaction
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.NON_PRIVILEGED
import dev.kord.gateway.PrivilegedIntent
import dev.kord.gateway.builder.PresenceBuilder
import dev.kord.gateway.builder.Shards
import dev.kord.rest.builder.message.EmbedBuilder.Field
import dev.kord.rest.builder.message.allowedMentions
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kordex.core.DATA_COLLECTION
import dev.kordex.core.DEV_MODE
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.KORDEX_VERSION
import dev.kordex.core.KORD_VERSION
import dev.kordex.core.annotations.BotBuilderDSL
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.annotations.tooling.Translatable
import dev.kordex.core.annotations.tooling.TranslatableType
import dev.kordex.core.checks.types.ChatCommandCheck
import dev.kordex.core.checks.types.MessageCommandCheck
import dev.kordex.core.checks.types.SlashCommandCheck
import dev.kordex.core.checks.types.UserCommandCheck
import dev.kordex.core.commands.application.ApplicationCommandRegistry
import dev.kordex.core.commands.application.DefaultApplicationCommandRegistry
import dev.kordex.core.commands.chat.ChatCommandRegistry
import dev.kordex.core.components.ComponentRegistry
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.impl.AboutExtension
import dev.kordex.core.i18n.ResourceBundleTranslations
import dev.kordex.core.i18n.SupportedLocales
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.koin.KordExContext
import dev.kordex.core.plugins.KordExPlugin
import dev.kordex.core.plugins.PluginManager
import dev.kordex.core.sentry.SentryAdapter
import dev.kordex.core.sentry.captures.SentryCapture
import dev.kordex.core.storage.DataAdapter
import dev.kordex.core.storage.toml.TomlDataAdapter
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.getKoin
import dev.kordex.core.utils.loadModule
import dev.kordex.core.utils.toReaction
import dev.kordex.data.api.DataCollection
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.sentry.SentryLevel
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.logger.Level
import org.koin.dsl.bind
import org.koin.environmentProperties
import org.koin.fileProperties
import org.koin.logger.slf4jLogger
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.div
import dev.kord.common.Locale as KLocale

internal typealias LocaleResolver = suspend (
	guild: GuildBehavior?,
	channel: ChannelBehavior?,
	user: UserBehavior?,
	interaction: Interaction?,
) -> Locale?

internal typealias FailureResponseBuilder =
	suspend (MessageCreateBuilder).(message: String, type: FailureReason<*>) -> Unit

internal typealias SentryDataTypeBuilder =
	ExtensibleBotBuilder.ExtensionsBuilder.SentryExtensionBuilder.SentryExtensionDataTypeBuilder

internal typealias SentryDataTypeTransformer =
	suspend (SentryDataTypeBuilder).(SentryCapture) -> Unit

/**
 * Builder class used for configuring and creating an [ExtensibleBot].
 *
 * This is a one-stop-shop for pretty much everything you could possibly need to change to configure your bot, via
 * properties and a bunch of DSL functions.
 */
@BotBuilderDSL
public open class ExtensibleBotBuilder {
	protected val logger: KLogger = KotlinLogging.logger {}

	/** Called to create an [ExtensibleBot], can be set to the constructor of your own subtype if needed. **/
	public var constructor: (ExtensibleBotBuilder, String) -> ExtensibleBot = ::ExtensibleBot

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public val aboutBuilder: AboutBuilder = AboutBuilder()

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public val cacheBuilder: CacheBuilder = CacheBuilder()

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public val componentsBuilder: ComponentsBuilder = ComponentsBuilder()

	/**
	 * @suppress Builder that shouldn't be set directly by the user.
	 */
	public var dataAdapterCallback: () -> DataAdapter<*> = ::TomlDataAdapter

	/**
	 * @suppress Builder that shouldn't be set directly by the user.
	 */
	public var failureResponseBuilder: FailureResponseBuilder = { message, _ ->
		allowedMentions { }

		content = message
	}

	/**
	 * Whether the bot is running in development mode.
	 *
	 * By default, KordEx determines this by checking, in order:
	 * * For the `devMode` property.
	 * * For the `DEV_MODE` env var.
	 * * Whether the `ENVIRONMENT` env var is "dev" or "development".
	 *
	 * If none of these options work for you, you can set this property yourself.
	 */
	@OptIn(InternalAPI::class)
	public var devMode: Boolean = DEV_MODE

	/**
	 * Data collection mode, usually configured in other ways, but you may override that configuration here.
	 *
	 * Note: Changing this value at runtime won't do anything.
	 *
	 * By default, KordEx determines this by checking, in order:
	 * * The value of the `dataCollection` property.
	 * * The value of the `DATA_COLLECTION` env var.
	 * * The value of the `settings.dataCollection` property in your bot's `kordex.properties` file, typically
	 *   generated by the Kord Extensions Gradle plugin.
	 *
	 * The values checked above must be one of "none", "minimal", "standard" or "extra" to be valid, or KordEx will
	 * throw an exception.
	 *
	 * If all the above values are missing, this setting defaults to "standard".
	 *
	 * For more information on what data KordEx collects, how to get at it, and how it's stored, please see here:
	 * TODO: LINK HERE
	 */
	@OptIn(InternalAPI::class)
	public var dataCollectionMode: DataCollection = DATA_COLLECTION

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public open val extensionsBuilder: ExtensionsBuilder = ExtensionsBuilder()

	/** @suppress Used for late execution of extensions builder calls, so plugins can be loaded first. **/
	protected open val deferredExtensionsBuilders: MutableList<suspend ExtensionsBuilder.() -> Unit> =
		mutableListOf()

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public val hooksBuilder: HooksBuilder = HooksBuilder()

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public val i18nBuilder: I18nBuilder = I18nBuilder()

	/** @suppress Plugin builder. **/
	public val pluginBuilder: PluginBuilder = PluginBuilder(this)

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public var intentsBuilder: (Intents.Builder.() -> Unit)? = {
		+Intents.NON_PRIVILEGED

		if (chatCommandsBuilder.enabled) {
			+Intent.MessageContent
		}

		getKoin().get<ExtensibleBot>().extensions.values.forEach { extension ->
			extension.intents.forEach {
				+it
			}
		}
	}

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public val membersBuilder: MembersBuilder = MembersBuilder()

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public val chatCommandsBuilder: ChatCommandsBuilder = ChatCommandsBuilder()

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public var presenceBuilder: PresenceBuilder.() -> Unit = { status = PresenceStatus.Online }

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public var shardingBuilder: ((recommended: Int) -> Shards)? = null

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public val applicationCommandsBuilder: ApplicationCommandsBuilder = ApplicationCommandsBuilder()

	/** @suppress List of Kord builders, shouldn't be set directly by the user. **/
	public val kordHooks: MutableList<suspend KordBuilder.() -> Unit> = mutableListOf()

	/** @suppress Kord builder, creates a Kord instance. **/
	public var kordBuilder: suspend (token: String, builder: suspend KordBuilder.() -> Unit) -> Kord =
		{ token, builder ->
			Kord(token) { builder() }
		}

	/** Logging level Koin should use, defaulting to ERROR. **/
	public var koinLogLevel: Level = Level.ERROR

	/**
	 * DSL function used to configure information about the bot.
	 *
	 * @see AboutBuilder
	 */
	@BotBuilderDSL
	public suspend fun about(builder: suspend AboutBuilder.() -> Unit) {
		builder(aboutBuilder)
	}

	/**
	 * DSL function used to configure the bot's caching options.
	 *
	 * @see CacheBuilder
	 */
	@BotBuilderDSL
	public suspend fun cache(builder: suspend CacheBuilder.() -> Unit) {
		builder(cacheBuilder)
	}

	/**
	 * Call this to register a custom data adapter class. Generally you'd pass a constructor here, but you can
	 * also provide a lambda if needed.
	 */
	@BotBuilderDSL
	public fun dataAdapter(builder: () -> DataAdapter<*>) {
		dataAdapterCallback = builder
	}

	/**
	 * DSL function used to configure the bot's plugin loading options.
	 *
	 * @see PluginBuilder
	 */
	@BotBuilderDSL
	public suspend fun plugins(builder: suspend PluginBuilder.() -> Unit) {
		builder(pluginBuilder)
	}

	/**
	 * DSL function used to configure the bot's components system.
	 *
	 * @see ComponentsBuilder
	 */
	@BotBuilderDSL
	public suspend fun components(builder: suspend ComponentsBuilder.() -> Unit) {
		builder(componentsBuilder)
	}

	/**
	 * Register the message builder responsible for formatting error responses, which are sent to users during command
	 * and component body execution.
	 */
	@BotBuilderDSL
	public fun errorResponse(builder: FailureResponseBuilder) {
		failureResponseBuilder = builder
	}

	/**
	 * DSL function used to insert code at various points in the bot's lifecycle.
	 *
	 * @see HooksBuilder
	 */
	@BotBuilderDSL
	public suspend fun hooks(builder: suspend HooksBuilder.() -> Unit) {
		builder(hooksBuilder)
	}

	/**
	 * DSL function allowing for additional Kord configuration builders to be specified, allowing for direct
	 * customisation of the Kord object.
	 *
	 * Multiple builders may be registered, and they'll be called in the order they were registered here. Builders are
	 * called after Kord Extensions has applied its own builder actions - so you can override the changes it makes here
	 * if they don't suit your bot.
	 *
	 * @see KordBuilder
	 */
	@BotBuilderDSL
	public fun kord(builder: suspend KordBuilder.() -> Unit) {
		kordHooks.add(builder)
	}

	/**
	 * Function allowing you to specify a callable that constructs and returns a Kord instance. This can be used
	 * to specify your own Kord subclass, if you need to - but shouldn't be a replacement for registering a [kord]
	 * configuration builder.
	 *
	 * @see Kord
	 */
	@BotBuilderDSL
	public fun customKordBuilder(builder: suspend (token: String, builder: suspend KordBuilder.() -> Unit) -> Kord) {
		kordBuilder = builder
	}

	/**
	 * DSL function used to configure the bot's chat command options.
	 *
	 * @see ChatCommandsBuilder
	 */
	@BotBuilderDSL
	public suspend fun chatCommands(builder: suspend ChatCommandsBuilder.() -> Unit) {
		builder(chatCommandsBuilder)
	}

	/**
	 * DSL function used to configure the bot's application command options.
	 *
	 * @see ApplicationCommandsBuilder
	 */
	@BotBuilderDSL
	public suspend fun applicationCommands(builder: suspend ApplicationCommandsBuilder.() -> Unit) {
		builder(applicationCommandsBuilder)
	}

	/**
	 * DSL function used to configure the bot's extension options, and add extensions. Calls to this function **do not
	 * run immediately**, so that plugins can be loaded beforehand.
	 *
	 * @see ExtensionsBuilder
	 */
	@BotBuilderDSL
	public open suspend fun extensions(builder: suspend ExtensionsBuilder.() -> Unit) {
		deferredExtensionsBuilders.add(builder)
	}

	/**
	 * DSL function used to configure the bot's intents.
	 *
	 * @param addDefaultIntents Whether to automatically add all non-privileged intents to the builder before running
	 * the given lambda.
	 * @param addDefaultIntents Whether to automatically add the required intents defined within each loaded extension
	 *
	 * @see Intents.Builder
	 */
	@BotBuilderDSL
	public fun intents(
		addDefaultIntents: Boolean = true,
		addExtensionIntents: Boolean = true,
		builder: Intents.Builder.() -> Unit,
	) {
		this.intentsBuilder = {
			if (addDefaultIntents) {
				+Intents.NON_PRIVILEGED

				if (chatCommandsBuilder.enabled) {
					+Intent.MessageContent
				}
			}

			if (addExtensionIntents) {
				getKoin().get<ExtensibleBot>().extensions.values.forEach { extension ->
					extension.intents.forEach {
						+it
					}
				}
			}

			builder()
		}
	}

	/**
	 * DSL function used to configure the bot's i18n settings.
	 *
	 * @see I18nBuilder
	 */
	@BotBuilderDSL
	public suspend fun i18n(builder: suspend I18nBuilder.() -> Unit) {
		builder(i18nBuilder)
	}

	/**
	 * DSL function used to configure the bot's member-related options.
	 *
	 * @see MembersBuilder
	 */
	@BotBuilderDSL
	public suspend fun members(builder: suspend MembersBuilder.() -> Unit) {
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

	/**
	 * DSL function used to configure the bot's sharding settings.
	 *
	 * @see dev.kord.core.builder.kord.KordBuilder.shardsBuilder
	 */
	@BotBuilderDSL
	public fun sharding(shards: (recommended: Int) -> Shards) {
		this.shardingBuilder = shards
	}

	/** @suppress Internal function used to initially set up Koin. **/
	public open suspend fun setupKoin() {
		startKoinIfNeeded()

		hooksBuilder.runBeforeKoinSetup()

		addBotKoinModules()

		hooksBuilder.runAfterKoinSetup()
	}

	/** @suppress Creates a new KoinApplication if it has not already been started. **/
	@OptIn(KoinInternalApi::class)
	private fun startKoinIfNeeded() {
		var logLevel = koinLogLevel

		if (logLevel == Level.INFO || logLevel == Level.DEBUG) {
			// NOTE: Temporary workaround for Koin not supporting Kotlin 1.6
			logLevel = Level.ERROR
		}

		if (koinNotStarted()) {
			KordExContext.startKoin {
				slf4jLogger(logLevel)
				environmentProperties()

				if (File("koin.properties").exists()) {
					fileProperties("koin.properties")
				}
			}
		} else {
			getKoin().logger.level = logLevel
		}
	}

	/** @suppress Internal function that checks if Koin has been started. **/
	private fun koinNotStarted(): Boolean = KordExContext.getOrNull() == null

	/**
	 * @suppress Internal function that creates and loads the bot's main Koin modules.
	 * The modules provide important bot-related singletons.
	 **/
	private fun addBotKoinModules() {
		loadModule { single { this@ExtensibleBotBuilder } bind ExtensibleBotBuilder::class }
		loadModule { single { i18nBuilder.translationsProvider } bind TranslationsProvider::class }
		loadModule { single { chatCommandsBuilder.registryBuilder() } bind ChatCommandRegistry::class }
		loadModule { single { componentsBuilder.registryBuilder() } bind ComponentRegistry::class }

		loadModule {
			single {
				applicationCommandsBuilder.applicationCommandRegistryBuilder()
			} bind ApplicationCommandRegistry::class
		}

		loadModule {
			single {
				val adapter = extensionsBuilder.sentryExtensionBuilder.builder()

				if (extensionsBuilder.sentryExtensionBuilder.enable) {
					extensionsBuilder.sentryExtensionBuilder.setupCallback(adapter)
				}

				adapter
			} bind SentryAdapter::class
		}
	}

	/** @suppress Plugin-loading function. **/
	@Suppress("TooGenericExceptionCaught")
	public open suspend fun loadPlugins() {
		val manager = pluginBuilder.manager(pluginBuilder.pluginPaths)

		loadModule { single { manager } bind PluginManager::class }

		pluginBuilder.managerObj = manager
		pluginBuilder.disabledPlugins.forEach(manager::disablePlugin)

		manager.loadPlugins()

		manager.plugins.forEach { wrapper ->
			try {
				val plugin = wrapper.plugin as? KordExPlugin

				plugin?.settingsCallbacks?.forEach { callback ->
					try {
						callback(this)
					} catch (e: Error) {
						logger.error(e) { "Error thrown while running settings callbacks for plugin: ${wrapper.pluginId}" }
					}
				}

				logger.info { "Loaded plugin: ${wrapper.pluginId} from ${wrapper.pluginPath}" }
			} catch (e: Error) {
				logger.error(e) { "Failed to load plugin: ${wrapper.pluginId} from ${wrapper.pluginPath}" }
			}
		}
	}

	/** @suppress Plugin-loading function. **/
	public open suspend fun startPlugins() {
		pluginBuilder.managerObj.startPlugins()
	}

	/** @suppress Internal function used to build a bot instance. **/
	public open suspend fun build(token: String): ExtensibleBot {
		logger.info { "Starting bot with Kord Extensions v$KORDEX_VERSION and Kord v$KORD_VERSION" }

		hooksBuilder.beforeKoinSetup {  // We have to do this super-duper early for safety
			loadModule { single { dataAdapterCallback() } bind DataAdapter::class }
		}

		hooksBuilder.beforeKoinSetup {
			if (pluginBuilder.enabled) {
				loadPlugins()
			}

			deferredExtensionsBuilders.forEach { it(extensionsBuilder) }
		}

		setupKoin()

		val bot = constructor(this, token)

		loadModule { single { bot } bind ExtensibleBot::class }

		hooksBuilder.runCreated(bot)

		bot.setup()

		hooksBuilder.runSetup(bot)
		hooksBuilder.runBeforeExtensionsAdded(bot)

		bot.addExtension(::AboutExtension)

		@Suppress("TooGenericExceptionCaught")
		extensionsBuilder.extensions.forEach {
			try {
				bot.addExtension(it)
			} catch (e: Exception) {
				logger.error(e) {
					"Failed to set up extension: $it"
				}
			}
		}

		if (pluginBuilder.enabled) {
			startPlugins()
		}

		hooksBuilder.runAfterExtensionsAdded(bot)

		return bot
	}

	/**
	 * Builder used for configuring the information provided by the about chat/slash command.
	 */
	@BotBuilderDSL
	public class AboutBuilder {
		/** Translation bundle used when translating your button names, bot name, and bot description. **/
		public var translationBundle: String? = null

		/** Whether to use ephemeral responses instead of public ones. **/
		public var ephemeral: Boolean = true

		/** Colour to use for the embed. **/
		public var color: Color = DISCORD_BLURPLE

		/** Translatable, a short description that explains what your bot is and does. **/
		public var description: String? = null

		/** Translatable, the name of your bot. **/
		public var name: String? = null

		/** A direct URL to your bot's logo, which should be at least 512 by 512, and either PNG or JPEG format. **/
		public var logoUrl: String? = null

		/** A URL pointing to a page that provides more information about your bot. **/
		public var url: String? = null

		/** Your bot's version number. **/
		public var version: String? = null

		internal val sections: MutableList<Section> = mutableListOf()
		internal val buttons: MutableList<Button> = mutableListOf()
		internal val fields: MutableList<suspend Field.() -> Unit> = mutableListOf()

		/**
		 * Add a field to the embed generated by the about command.
		 */
		public fun field(body: suspend Field.() -> Unit) {
			fields.add(body)
		}

		/**
		 * Add a custom about command subcommand, with your own custom content.
		 *
		 * If you decide to add sections, Kord Extensions will move the default about command defined in this
		 * configuration to a subcommand named "general".
		 */
		public fun section(body: Section.() -> Unit) {
			val section = Section(this)

			body(section)

			section.validate()

			sections.add(section)
		}

		/**
		 * Add a custom button to be displayed under your bot's about information.
		 *
		 * You may only add up to five (5) buttons.
		 */
		@Suppress("MagicNumber")
		public fun button(body: Button.() -> Unit) {
			if (buttons.size >= 5) {
				error("Failed to add button: Only a maximum of 5 buttons may be added")
			}

			val button = Button()

			body(button)

			buttons.add(button)
		}

		/** Convenience function for adding a button that links to your bot's documentation. **/
		public fun docsButton(url: String) {
			button {
				name = "extensions.about.buttons.docs"
				emoji = "📖".toReaction()

				this.url = url
			}
		}

		/** Convenience function for adding a button that links to your bot's donation page. **/
		public fun donationButton(url: String) {
			button {
				name = "extensions.about.buttons.donate"
				emoji = "💵".toReaction()

				this.url = url
			}
		}

		/** Convenience function for adding a button that links to your bot's invite page. **/
		public fun inviteButton(url: String) {
			button {
				name = "extensions.about.buttons.invite"
				emoji = "➕".toReaction()

				this.url = url
			}
		}

		/** Convenience function for adding a button that links to your bot's source code. **/
		public fun sourceButton(url: String) {
			button {
				name = "extensions.about.buttons.source"
				emoji = "🛠️".toReaction()

				this.url = url
			}
		}

		/** Convenience function for adding a button that links to your bot's website. **/
		public fun websiteButton(url: String) {
			button {
				name = "extensions.about.buttons.website"
				emoji = "🔗".toReaction()

				this.url = url
			}
		}

		public class Button {
			/** Translatable, the text to show on this button. **/
			public lateinit var name: String

			/** The URL this button should link to. **/
			public lateinit var url: String

			/** An emoji to show as this button's icon. **/
			public var emoji: ReactionEmoji? = null
		}

		public class Section(parent: AboutBuilder) {
			public lateinit var name: String
			public lateinit var description: String
			public lateinit var messageBuilder: suspend MessageCreateBuilder.() -> Unit

			/** Whether to use ephemeral responses instead of public ones. **/
			public var ephemeral: Boolean = true

			public var bundle: String? = parent.translationBundle

			public fun message(body: suspend MessageCreateBuilder.() -> Unit) {
				messageBuilder = body
			}

			public fun validate() {
				if (
					!::name.isInitialized ||
					!::description.isInitialized
				) {
					error("About command sections must contain a name and description.")
				}

				if (
					!::messageBuilder.isInitialized
				) {
					error(
						"About command sections must contain a message builder; use the `message` DSL function to " +
							"provide one."
					)
				}
			}
		}
	}

	/**
	 * Builder used for configuring the bot's wired-plugin-loading options.
	 *
	 * @property parent Parent [ExtensibleBotBuilder], for extension functions.
	 */
	@BotBuilderDSL
	public class PluginBuilder(public val parent: ExtensibleBotBuilder) {
		internal lateinit var managerObj: PluginManager

		/** Whether to attempt to load wired plugin. Defaults to `true`. **/
		public var enabled: Boolean = true

		/** Plugin manager builder, which you can replace if your needs require it. **/
		public var manager: (List<Path>) -> PluginManager = ::PluginManager

		/** List of paths to load plugin from. Uses `plugins/` in the current working directory by default. **/
		public val pluginPaths: MutableList<Path> = mutableListOf(
			Path(".") / "plugins"
		)

		/** List of plugin IDs to disable. Plugins in this list will not be loaded automatically. **/
		public val disabledPlugins: MutableList<String> = mutableListOf()

		/**
		 * Convenience function for disabling a plugin by ID.
		 *
		 * @see disabledPlugins
		 */
		public fun disable(id: String) {
			disabledPlugins.add(id)
		}

		/**
		 * Convenience function for adding a plugin path.
		 *
		 * @see pluginPaths
		 */
		public fun pluginPath(path: String) {
			pluginPaths.add(Path.of(path))
		}

		/**
		 * Convenience function for adding a plugin path.
		 *
		 * @see pluginPaths
		 */
		public fun pluginPath(path: Path) {
			pluginPaths.add(path)
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

		/** The default Kord caching strategy - defaults to caching REST when an entity doesn't exist in the cache. **/
		public var defaultStrategy: EntitySupplyStrategy<EntitySupplier> =
			EntitySupplyStrategy.cacheWithCachingRestFallback

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

	/** Builder used to configure the bot's components settings. **/
	@BotBuilderDSL
	public class ComponentsBuilder {

		/** @suppress Component registry builder. **/
		public var registryBuilder: () -> ComponentRegistry = ::ComponentRegistry

		/**
		 * Register a builder (usually a constructor) returning a [ComponentRegistry] instance, which may be useful
		 * if you need to register a custom subclass.
		 */
		public fun registry(builder: () -> ComponentRegistry) {
			registryBuilder = builder
		}
	}

	/** Builder used for configuring the bot's extension options, and registering custom extensions. **/
	@BotBuilderDSL
	public open class ExtensionsBuilder {
		/** @suppress Internal list that shouldn't be modified by the user directly. **/
		public open val extensions: MutableList<() -> Extension> = mutableListOf()

		/** @suppress Help extension builder. **/
		public open val helpExtensionBuilder: HelpExtensionBuilder = HelpExtensionBuilder()

		/** @suppress Sentry extension builder. **/
		public open val sentryExtensionBuilder: SentryExtensionBuilder = SentryExtensionBuilder()

		/** Add a custom extension to the bot via a builder - probably the extension constructor. **/
		public open fun add(builder: () -> Extension) {
			extensions.add(builder)
		}

		/** Configure the built-in help extension, or disable it so you can use your own. **/
		public open suspend fun help(builder: HelpExtensionBuilder.() -> Unit) {
			builder(helpExtensionBuilder)
		}

		/** Configure the built-in sentry extension, or disable it so you can use your own. **/
		public open suspend fun sentry(builder: SentryExtensionBuilder.() -> Unit) {
			builder(sentryExtensionBuilder)
		}

		/** Builder used to configure Sentry and the Sentry extension. **/
		@BotBuilderDSL
		public open class SentryExtensionBuilder {
			/** Whether to enable Sentry integration. This includes the extension, and [SentryAdapter] setup. **/
			public open var enable: Boolean = false

			/**
			 * Whether to enable the Sentry extension, which provides feedback commands.
			 *
			 * This will be ignored if [enable] is `false`.
			 */
			public open var feedbackExtension: Boolean = false

			/** Whether to enable Sentry's debug mode. **/
			public open var debug: Boolean = false

			/** Your Sentry DSN, required for submitting events to Sentry. **/
			public open var dsn: String? = null

			/** Optional distribution name to send to Sentry. **/
			public open var distribution: String? = null

			/** Optional environment name to send to Sentry. **/
			public open var environment: String? = null

			/** Optional release version to send to Sentry. **/
			public open var release: String? = null

			/** Optional server name to send to Sentry. **/
			public open var serverName: String? = null

			/** Whether to ping users when responding to them. **/
			public var pingInReply: Boolean = true

			/** How many events to send to Sentry, as a percentage. Defaults to 1.0, meaning all events. **/
			public var sampleRate: Double = 1.0

			/** Builder used to construct a [SentryAdapter] instance, usually the constructor. **/
			public open var builder: () -> SentryAdapter = ::SentryAdapter

			/**
			 * Function in charge of setting up the [SentryAdapter], by calling its `setup` function. You can use this
			 * if you need to pass extra parameters to the setup function, but make sure you pass everything that's
			 * required.
			 */
			public open var setupCallback: SentryAdapter.() -> Unit = {
				this.init { options ->
					options.dsn = dsn
					options.isDebug = debug

					options.dist = distribution
					options.environment = environment
					options.release = release
					options.serverName = serverName

					options.isAttachThreads = false
					options.sampleRate = sampleRate

					options.setDiagnosticLevel(SentryLevel.WARNING)
				}
			}

			/** @suppress Storage for Sentry datatype settings container. **/
			public val dataTypeBuilder: SentryExtensionDataTypeBuilder = SentryExtensionDataTypeBuilder()

			/** @suppress Storage for Sentry datatype transformers. **/
			public val dataTypeTransformers: MutableList<SentryDataTypeTransformer> = mutableListOf()

			/** @suppress Storage for Sentry submission predicates. **/
			public val predicates: MutableList<suspend SentryCapture.() -> Boolean> = mutableListOf()

			/**
			 * Register a Sentry submission predicate.
			 *
			 * You can use a submission predicate to filter which captures Kord Extensions sends to Sentry.
			 * Return `false` from your predicate to prevent the capture from being sent.
			 *
			 * The Sentry adapter will iterate the predicates in order of definition, stopping when one returns `false`
			 * or there's none left.
			 */
			public fun predicate(body: suspend SentryCapture.() -> Boolean) {
				predicates.add(body)
			}

			/**
			 * Configure the default data types sent via (or omitted from) Sentry captures.
			 */
			public fun defaultDataTypes(body: SentryExtensionDataTypeBuilder.() -> Unit) {
				body(dataTypeBuilder)
			}

			/**
			 * Registry a Sentry data type transformer.
			 *
			 * This allows you to dynamically change which data types are sent to Sentry, depending on the given capture
			 * object.
			 *
			 * Transformers may modify a clone of the default [SentryExtensionDataTypeBuilder] freely, which will be
			 * based on the one configured via [defaultDataTypes].
			 */
			public fun dataTypeTransformer(body: SentryDataTypeTransformer) {
				dataTypeTransformers.add(body)
			}

			/** Register a builder used to construct a [SentryAdapter] instance, usually the constructor. **/
			public fun builder(body: () -> SentryAdapter) {
				builder = body
			}

			/**
			 * Convenience function to enable and set the DSN, but only if the supplied DSN isn't null.
			 *
			 * Intended for use with `envOrNull`.
			 */
			public fun enableIfDSN(sentryDSN: String?) {
				if (sentryDSN != null) {
					dsn = sentryDSN
					enable = true
				}
			}

			/**
			 * Register the function in charge of setting up the [SentryAdapter], by calling its `setup` function.
			 * You can use this if you need to pass extra parameters to the setup function, but make sure you pass
			 * everything that's required.
			 */
			public fun setup(body: SentryAdapter.() -> Unit) {
				setupCallback = body
			}

			public class SentryExtensionDataTypeBuilder {
				/** Whether to send command arguments to Sentry. **/
				public var arguments: Boolean = true

				/** Whether to send channel information to Sentry. **/
				public var channels: Boolean = true

				/** Whether to send guild information to Sentry. **/
				public var guilds: Boolean = true

				/** Whether to send guild information to Sentry. **/
				public var roles: Boolean = true

				/** Whether to send user information to Sentry. **/
				public var users: Boolean = true

				/** Comma-separated list of omitted data types, based on this object's configured booleans. **/
				public val omittedData: String? by lazy {
					val omitted: MutableList<String> = mutableListOf()

					if (!arguments) {
						omitted.add("arguments")
					}

					if (!channels) {
						omitted.add("channels")
					}

					if (!guilds) {
						omitted.add("guilds")
					}

					if (!roles) {
						omitted.add("roles")
					}

					if (!users) {
						omitted.add("users")
					}

					if (omitted.isEmpty()) {
						null
					} else {
						omitted.joinToString()
					}
				}

				/** Clone this object, allowing for just-in-time customisation where needed. **/
				public fun clone(): SentryExtensionDataTypeBuilder {
					val other = SentryExtensionDataTypeBuilder()

					other.arguments = this.arguments
					other.channels = this.channels
					other.guilds = this.guilds
					other.users = this.users

					return other
				}
			}
		}

		/** Builder used for configuring options, specifically related to the help extension. **/
		@BotBuilderDSL
		public open class HelpExtensionBuilder {
			/** Whether to enable the bundled help extension. Defaults to `true`. **/
			public var enableBundledExtension: Boolean = true

			/**
			 * Time to wait before the help paginator times out and can't be used, in seconds. Defaults to 60.
			 */
			@Suppress("MagicNumber")
			public var paginatorTimeout: Long = 60L  // 60 seconds

			/** Whether to delete the help paginator after the timeout ends. **/
			public var deletePaginatorOnTimeout: Boolean = false

			/** Whether to delete the help command invocation after the paginator timeout ends. **/
			public var deleteInvocationOnPaginatorTimeout: Boolean = false

			/** Whether to ping users when responding to them. **/
			public var pingInReply: Boolean = true

			/** List of command checks. These checks will be checked against all commands in the help extension. **/
			public val checkList: MutableList<ChatCommandCheck> = mutableListOf()

			/** For custom help embed colours. Only one may be defined. **/
			public var colourGetter: suspend MessageCreateEvent.() -> Color = { DISCORD_BLURPLE }

			/** Define a callback that returns a [Color] to use for help embed colours. Feel free to mix it up! **/
			public fun colour(builder: suspend MessageCreateEvent.() -> Color) {
				colourGetter = builder
			}

			/** Like [colour], but American. **/
			public fun color(builder: suspend MessageCreateEvent.() -> Color): Unit = colour(builder)

			/**
			 * Define a check which must pass for help commands to be executed. This check will be applied to all
			 * commands in the extension.
			 *
			 * A command may have multiple checks - all checks must pass for the command to be executed.
			 * Checks will be run in the order that they're defined.
			 *
			 * This function can be used DSL-style with a given body, or it can be passed one or more
			 * predefined functions. See the samples for more information.
			 *
			 * @param checks Checks to apply to all help commands.
			 */
			public fun check(vararg checks: ChatCommandCheck) {
				checks.forEach { checkList.add(it) }
			}

			/**
			 * Overloaded check function to allow for DSL syntax.
			 *
			 * @param check Check to apply to all help commands.
			 */
			public fun check(check: ChatCommandCheck) {
				checkList.add(check)
			}
		}
	}

	/** Builder used to insert code at various points in the bot's lifecycle. **/
	@Suppress("TooGenericExceptionCaught")
	// We need to catch literally everything in here
	@BotBuilderDSL
	public class HooksBuilder {
		/**
		 * Whether Kord's shutdown hook should be registered. When enabled, Kord logs out of the gateway on shutdown.
		 */
		public var kordShutdownHook: Boolean = true

		// region: Hook lists

		/** @suppress Internal list of hooks. **/
		public val afterExtensionsAddedList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

		/** @suppress Internal list of hooks. **/
		public val afterKoinSetupList: MutableList<suspend () -> Unit> = mutableListOf()

		/** @suppress Internal list of hooks. **/
		public val beforeKoinSetupList: MutableList<suspend () -> Unit> = mutableListOf()

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
		@BotBuilderDSL
		public fun afterExtensionsAdded(body: suspend ExtensibleBot.() -> Unit): Boolean =
			afterExtensionsAddedList.add(body)

		/**
		 * Register a lambda to be called after Koin has been set up. You can use this to register overriding modules
		 * via `loadModule` before the modules are actually accessed.
		 */
		@BotBuilderDSL
		public fun afterKoinSetup(body: suspend () -> Unit): Boolean =
			afterKoinSetupList.add(body)

		/**
		 * Register a lambda to be called before Koin has been set up. You can use this to register Koin modules
		 * early, if needed.
		 */
		@BotBuilderDSL
		public fun beforeKoinSetup(body: suspend () -> Unit): Boolean =
			beforeKoinSetupList.add(body)

		/**
		 * Register a lambda to be called before all the extensions in the [ExtensionsBuilder] have been added.
		 */
		@BotBuilderDSL
		public fun beforeExtensionsAdded(body: suspend ExtensibleBot.() -> Unit): Boolean =
			beforeExtensionsAddedList.add(body)

		/**
		 * Register a lambda to be called just before the bot tries to connect to Discord.
		 */
		@BotBuilderDSL
		public fun beforeStart(body: suspend ExtensibleBot.() -> Unit): Boolean =
			beforeStartList.add(body)

		/**
		 * Register a lambda to be called right after the [ExtensibleBot] object has been created, before it gets set
		 * up.
		 */
		@BotBuilderDSL
		public fun created(body: suspend ExtensibleBot.() -> Unit): Boolean =
			createdList.add(body)

		/**
		 * Register a lambda to be called after any extension is successfully added to the bot.
		 */
		@BotBuilderDSL
		public fun extensionAdded(body: suspend ExtensibleBot.(extension: Extension) -> Unit): Boolean =
			extensionAddedList.add(body)

		/**
		 * Register a lambda to be called after the [ExtensibleBot] object has been created and set up.
		 */
		@BotBuilderDSL
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
		public suspend fun runAfterKoinSetup() {
			val logger: KLogger = KotlinLogging.logger {}

			afterKoinSetupList.forEach {
				try {
					it.invoke()
				} catch (t: Throwable) {
					logger.error(t) {
						"Failed to run afterKoinSetup hook $it"
					}
				}
			}
		}

		/** @suppress Internal hook execution function. **/
		public suspend fun runBeforeKoinSetup() {
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
		@Translatable(TranslatableType.LOCALE)
		public var defaultLocale: Locale = SupportedLocales.ENGLISH

		/**
		 * List of [locales][KLocale] which are used for application command names (without [defaultLocale]).
		 */
		public var applicationCommandLocales: MutableList<KLocale> = mutableListOf()

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

		/**
		 * Registers [locales] as application command languages.
		 *
		 * **Do not register [defaultLocale]!**
		 */
		@JvmName("applicationCommandLocale_v1")
		public fun applicationCommandLocale(
			@Translatable(TranslatableType.LOCALE)
			vararg locales: KLocale,
		) {
			applicationCommandLocales.addAll(locales.toList())
		}

		/**
		 * Registers [locales] as application command languages.
		 *
		 * **Do not register [defaultLocale]!**
		 */
		@JvmName("applicationCommandLocale_v2")
		public fun applicationCommandLocale(
			@Translatable(TranslatableType.LOCALE)
			vararg locales: Locale,
		) {
			applicationCommandLocales.addAll(locales.map { it.kLocale })
		}

		/**
		 * Registers [locales] as application command languages.
		 *
		 * **Do not register [defaultLocale]!**
		 */
		@JvmName("applicationCommandLocale_c1")
		public fun applicationCommandLocale(
			@Translatable(TranslatableType.LOCALE)
			locales: Collection<KLocale>,
		) {
			applicationCommandLocales.addAll(locales)
		}

		/**
		 * Registers [locales] as application command languages.
		 *
		 * **Do not register [defaultLocale]!**
		 */
		@JvmName("applicationCommandLocale_c2")
		public fun applicationCommandLocale(
			@Translatable(TranslatableType.LOCALE)
			locales: Collection<Locale>,
		) {
			applicationCommandLocales.addAll(locales.map { it.kLocale })
		}

		/**
		 * Registers a [LocaleResolver] using [Interaction.locale].
		 */
		public fun interactionUserLocaleResolver(): Unit =
			localeResolver { _, _, _, interaction ->
				interaction?.locale?.asJavaLocale()
			}

		/**
		 * Registers a [LocaleResolver] using [Interaction.guildLocale].
		 */
		public fun interactionGuildLocaleResolver(): Unit =
			localeResolver { _, _, _, interaction ->
				interaction?.guildLocale?.asJavaLocale()
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
		 * Whether to lock when requesting members from guilds, preventing concurrent requests from being processed
		 * at once. This will slow down filling the cache with members, but may avoid hitting rate limits for larger
		 * bots.
		 */
		public var lockMemberRequests: Boolean = false

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
		public fun fill(ids: Collection<ULong>): Boolean? =
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
		public fun fill(id: ULong): Boolean? =
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

	/** Builder used for configuring the bot's chat command options. **/
	@BotBuilderDSL
	public class ChatCommandsBuilder {
		/** Whether to invoke commands on bot mentions, in addition to using chat prefixes. Defaults to `true`. **/
		public var invokeOnMention: Boolean = true

		/** Prefix to require for command invocations on Discord. Defaults to `"!"`. **/
		public var defaultPrefix: String = "!"

		/** Whether to register and process chat commands. Defaults to `false`. **/
		public var enabled: Boolean = false

		/** @suppress Builder that shouldn't be set directly by the user. **/
		public var prefixCallback: suspend (MessageCreateEvent).(String) -> String = { defaultPrefix }

		/** @suppress Builder that shouldn't be set directly by the user. **/
		public var registryBuilder: () -> ChatCommandRegistry = { ChatCommandRegistry() }

		/** Whether to ignore command invocations in messages sent by the bot. Defaults to `true`. **/
		public var ignoreSelf: Boolean = true

		/**
		 * List of command checks.
		 *
		 * These checks will be checked against all commands.
		 */
		public val checkList: MutableList<ChatCommandCheck> = mutableListOf()

		/**
		 * Register a lambda that takes a [MessageCreateEvent] object and the default prefix, and returns the
		 * command prefix to be made use of for that message event.
		 *
		 * This is intended to allow for different chat command prefixes in different contexts - for example,
		 * guild-specific prefixes.
		 */
		public fun prefix(builder: suspend (MessageCreateEvent).(String) -> String) {
			prefixCallback = builder
		}

		/**
		 * Register the builder used to create the [ChatCommandRegistry]. You can change this if you need to
		 * make use of a subclass.
		 */
		public fun registry(builder: () -> ChatCommandRegistry) {
			registryBuilder = builder
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
		public fun check(vararg checks: ChatCommandCheck) {
			checks.forEach { checkList.add(it) }
		}

		/**
		 * Overloaded check function to allow for DSL syntax.
		 *
		 * @param check Checks to apply to all commands.
		 */
		public fun check(check: ChatCommandCheck) {
			checkList.add(check)
		}
	}

	/** Builder used for configuring the bot's application command options. **/
	@BotBuilderDSL
	public class ApplicationCommandsBuilder {
		/** Whether to register and process application commands. Defaults to `true`. **/
		public var enabled: Boolean = true

		/** The guild ID to use for all global application commands. Intended for testing. **/
		public var defaultGuild: Snowflake? = null

		/** Whether to attempt to register the bot's application commands. Intended for multi-instance sharded bots. **/
		public var register: Boolean = true

		/** @suppress Builder that shouldn't be set directly by the user. **/
		public var applicationCommandRegistryBuilder: () -> ApplicationCommandRegistry =
			{ DefaultApplicationCommandRegistry() }

		/**
		 * List of message command checks.
		 *
		 * These checks will be checked against all message commands.
		 */
		public val messageCommandChecks: MutableList<MessageCommandCheck> = mutableListOf()

		/**
		 * List of slash command checks.
		 *
		 * These checks will be checked against all slash commands.
		 */
		public val slashCommandChecks: MutableList<SlashCommandCheck> = mutableListOf()

		/**
		 * List of user command checks.
		 *
		 * These checks will be checked against all user commands.
		 */
		public val userCommandChecks: MutableList<UserCommandCheck> = mutableListOf()

		/** Set a guild ID to use for all global application commands. Intended for testing. **/
		public fun defaultGuild(id: Snowflake?) {
			defaultGuild = id
		}

		/** Set a guild ID to use for all global application commands. Intended for testing. **/
		public fun defaultGuild(id: ULong?) {
			defaultGuild = id?.let { Snowflake(it) }
		}

		/** Set a guild ID to use for all global application commands. Intended for testing. **/
		public fun defaultGuild(id: String?) {
			defaultGuild = id?.let { Snowflake(it) }
		}

		/**
		 * Register the builder used to create the [ApplicationCommandRegistry]. You can change this if you need to make
		 * use of a subclass.
		 */
		public fun applicationCommandRegistry(builder: () -> ApplicationCommandRegistry) {
			applicationCommandRegistryBuilder = builder
		}

		/**
		 * Define a check which must pass for a message command to be executed. This check will be applied to all
		 * message commands.
		 *
		 * A message command may have multiple checks - all checks must pass for the command to be executed.
		 * Checks will be run in the order that they're defined.
		 *
		 * This function can be used DSL-style with a given body, or it can be passed one or more
		 * predefined functions. See the samples for more information.
		 *
		 * @param checks Checks to apply to all slash commands.
		 */
		public fun messageCommandCheck(vararg checks: MessageCommandCheck) {
			checks.forEach { messageCommandChecks.add(it) }
		}

		/**
		 * Overloaded message command check function to allow for DSL syntax.
		 *
		 * @param check Check to apply to all slash commands.
		 */
		public fun messageCommandCheck(check: MessageCommandCheck) {
			messageCommandChecks.add(check)
		}

		/**
		 * Define a check which must pass for a slash command to be executed. This check will be applied to all
		 * slash commands.
		 *
		 * A slash command may have multiple checks - all checks must pass for the command to be executed.
		 * Checks will be run in the order that they're defined.
		 *
		 * This function can be used DSL-style with a given body, or it can be passed one or more
		 * predefined functions. See the samples for more information.
		 *
		 * @param checks Checks to apply to all slash commands.
		 */
		public fun slashCommandCheck(vararg checks: SlashCommandCheck) {
			checks.forEach { slashCommandChecks.add(it) }
		}

		/**
		 * Overloaded slash command check function to allow for DSL syntax.
		 *
		 * @param check Check to apply to all slash commands.
		 */
		public fun slashCommandCheck(check: SlashCommandCheck) {
			slashCommandChecks.add(check)
		}

		/**
		 * Define a check which must pass for a user command to be executed. This check will be applied to all
		 * user commands.
		 *
		 * A user command may have multiple checks - all checks must pass for the command to be executed.
		 * Checks will be run in the order that they're defined.
		 *
		 * This function can be used DSL-style with a given body, or it can be passed one or more
		 * predefined functions. See the samples for more information.
		 *
		 * @param checks Checks to apply to all slash commands.
		 */
		public fun userCommandCheck(vararg checks: UserCommandCheck) {
			checks.forEach { userCommandChecks.add(it) }
		}

		/**
		 * Overloaded user command check function to allow for DSL syntax.
		 *
		 * @param check Check to apply to all slash commands.
		 */
		public fun userCommandCheck(check: UserCommandCheck) {
			userCommandChecks.add(check)
		}
	}
}
