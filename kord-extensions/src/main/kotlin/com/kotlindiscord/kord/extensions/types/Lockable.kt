package com.kotlindiscord.kord.extensions.types

import kotlinx.coroutines.sync.Mutex

/** Interface representing something with a [Mutex] that can be locked. **/
public interface Lockable {
    /** Mutex object to use for locking. **/
    public var mutex: Mutex?

    /** Set this to `true` to lock execution with a [Mutex]. **/
    public var locking: Boolean

    /** Lock the mutex (if locking is enabled), call the supplied callable, and unlock. **/
    public suspend fun <T> withLock(body: suspend () -> T) {
        try {
            lock()

            body()
        } finally {
            unlock()
        }
    }

    /** Lock the mutex, if locking is enabled - suspending until it's unlocked. **/
    public suspend fun lock() {
        if (locking) {
            mutex?.lock()
        }
    }

    /** Unlock the mutex, if it's locked. **/
    public fun unlock() {
        if (locking) {
            mutex?.unlock()
        }
    }
}
