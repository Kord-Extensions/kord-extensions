package com.kotlindiscord.kord.extensions.extensions

/** Extension states, which describe what state of loading/unloading an extension is currently in. **/
public enum class ExtensionState {
    FAILED_LOADING,
    FAILED_UNLOADING,

    LOADED,
    LOADING,

    UNLOADED,
    UNLOADING,
}
