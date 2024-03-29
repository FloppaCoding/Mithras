@file:Suppress("UNCHECKED_CAST")

package floppacoding.oldui.events

import floppacoding.oldui.elements.ElementOLD

fun <E : ElementOLD> E.onClick(button: Int, block: Mouse.Clicked.() -> Boolean): E {
    registerEvent(Mouse.Clicked(button), block as Event.() -> Boolean)
    return this
}

fun <E : ElementOLD> E.onRelease(button: Int, block: Mouse.Released.() -> Unit): E {
    registerEvent(Mouse.Released(button)) {
        (this as Mouse.Released).block()
        true
    }
    return this
}

fun <E : ElementOLD> E.onMouseEnter(block: Mouse.Entered.() -> Boolean): E {
    registerEvent(Mouse.Entered, block as Event.() -> Boolean)
    return this
}

fun <E : ElementOLD> E.onMouseExit(block: Mouse.Exited.() -> Boolean): E {
    registerEvent(Mouse.Exited, block as Event.() -> Boolean)
    return this
}