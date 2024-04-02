@file:Suppress("UNCHECKED_CAST")

package floppacoding.ui.events

import floppacoding.ui.elements.Element

fun <E : Element> E.onClick(button: Int, block: Mouse.Clicked.() -> Boolean): E {
    registerEvent(Mouse.Clicked(button), false, block as Event.() -> Boolean)
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
    registerEvent(Mouse.Entered, false, block as Event.() -> Boolean)
    return this
}

fun <E : Element> E.onMouseExit(block: Mouse.Exited.() -> Boolean): E {
    registerEvent(Mouse.Exited, false, block as Event.() -> Boolean)
    return this
}

fun <E : Element> E.onMouseEnterExit(block: Event.() -> Boolean): E {
    registerEvent(Mouse.Entered, false, block)
    registerEvent(Mouse.Exited, false, block)
    return this
}

fun <E : Element> E.onMouseMove(block: Mouse.Moved.() -> Boolean): E {
    registerEvent(Mouse.Moved, false, block as Event.() -> Boolean)
    return this
}

// todo: maybe make key.typed always be equal as long as other is also key.typed?
fun <E : Element> E.onKeyType(keycode: Int? = null, block: Key.Typed.() -> Boolean): E {
    registerEvent(Key.Typed(keycode), false, block as Event.() -> Boolean)
    return this
}

fun <E : Element> E.onFocusGain(block: Focused.Gained.() -> Unit): E {
    registerEvent(Focused.Gained) {
        (this as Focused.Gained).block()
        true
    }
    return this
}

fun <E : Element> E.onFocusLost(block: Focused.Lost.() -> Unit): E {
    registerEvent(Focused.Lost) {
        Focused.Lost.block()
        true
    }
    return this
}