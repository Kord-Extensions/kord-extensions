package com.kotlindiscord.kord.extensions.components

import java.util.*

public abstract class ComponentWithID : Component() {
    public open var id: String = UUID.randomUUID().toString()

    public override fun validate() {
        if (id.isEmpty()) {
            error("All components must have a unique ID.")
        }
    }
}
