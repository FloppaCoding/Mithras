@file:Suppress("UNCHECKED_CAST")

package floppacoding.ui.events

import floppacoding.ui.elements.Element

fun <E : Element> E.onClick(button: Int, block: Mouse.Clicked.() -> Boolean): E {
    registerEvent(Mouse.Clicked(button), block as Event.() -> Boolean)
    return this
}

fun <E : Element> E.onRelease(button: Int, block: Mouse.Released.() -> Unit): E {
    registerEvent(Mouse.Released(button)) {
        (this as Mouse.Released).block()
        true
    }
    return this
}

fun <E : Element> E.onMouseEnter(block: Mouse.Entered.() -> Boolean): E {
    registerEvent(Mouse.Entered, block as Event.() -> Boolean)
    return this
}

fun <E : Element> E.onMouseExit(block: Mouse.Exited.() -> Boolean): E {
    registerEvent(Mouse.Exited, block as Event.() -> Boolean)
    return this
}

fun <E : Element> E.onMouseEnterExit(block: Event.() -> Boolean): E {
    registerEvent(Mouse.Entered, block)
    registerEvent(Mouse.Exited, block)
    return this
}