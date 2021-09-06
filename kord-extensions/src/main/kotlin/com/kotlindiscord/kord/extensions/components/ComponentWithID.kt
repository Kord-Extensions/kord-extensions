package com.kotlindiscord.kord.extensions.components

import java.util.*

/** Abstract class representing a component with an ID, which defaults to a newly-generated UUID. **/
public abstract class ComponentWithID : Component() {
    /** Component's ID, a UUID by default. **/
    public open var id: String = UUID.randomUUID().toString()

    public override fun validate() {
        if (id.isEmpty()) {
            error("All components must have a unique ID.")
        }
    }
}
