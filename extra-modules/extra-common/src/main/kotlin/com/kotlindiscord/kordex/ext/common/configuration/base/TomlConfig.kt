package com.kotlindiscord.kordex.ext.common.configuration.base

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.Feature
import com.uchuhimo.konf.Spec
import com.uchuhimo.konf.source.toml
import java.io.File

/**
 * Abstract class representing a toml configuration,
 *
 * It takes the following steps to resolve a configuration, with values from later steps overriding earlier ones:
 *
 * 1. Look for `resourcePrefix/baseName/default.toml` in the JAR's resources (you should ship this with your module)
 * 2. Look for `resourcePrefix/baseName/config.toml` in the JAR's resources (users can provide this in their dist)
 * 3. Look for `./config/configFolder/baseName.toml` on the filesystem (for users to write their own config files)
 * 4. Look for env vars prefixed with `RESOURCEPREFIX_BASENAME`
 * 5. Look for system properties prefixed with `resourcePrefix.baseName`
 *
 * This class contains the default behaviour for all KordEx default configurations. You can extend it along
 * with your configuration-specific interface, using the `config` property to retrieve whatever values you need.
 *
 * @param baseName Module/extension name in lowerCamelCase.
 * @param specs All of the config [Spec] objects that should be loaded, in the order they should be loaded in.
 * @param resourcePrefix Name for the resource group, "kordex" (by default) for all KordEx modules.
 * @param configFolder Name for the inner config folder, "ext" (by default) for KordEx extension modules.
 * @param configModifier Lambda that will be inserted into the `Config` object instantiation, if you need to
 * customize it.
 */
@Suppress("UnnecessaryAbstractClass")  // Well that's just not true at all
abstract class TomlConfig(
    baseName: String,
    vararg specs: Spec,
    resourcePrefix: String = "kordex",
    configFolder: String = "ext",
    configModifier: (Config.() -> Unit)? = null
) {
    /** Configuration object, use `config\[Spec.prop]` to retrieve values from the loaded configuration. **/
    var config = Config {
        specs.forEach { addSpec(it) }

        configModifier?.invoke(this)
    }
        .from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).toml.resource(
            "$resourcePrefix/$baseName/default.toml"
        )
        .from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).toml.resource(
            "$resourcePrefix/$baseName/config.toml",
            optional = true
        )

    init {
        if (File("config/$configFolder/$baseName.toml").exists()) {
            config = config.from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).toml.watchFile(
                "config/$configFolder/$baseName.toml",
                optional = true
            )
        }

        config = config
            .from.prefixed("${resourcePrefix.uppercase()}_${baseName.uppercase()}").env()
            .from.prefixed("$resourcePrefix.$baseName").systemProperties()
    }
}
