@file:Suppress("UNCHECKED_CAST")

package floppacoding.renameui.events

import floppacoding.renameui.elements.Element

fun <E : Element> E.onClick(button: Int, block: Mouse.Clicked.() -> Boolean) {
    registerEvent(Mouse.Clicked(button), block as Event.() -> Boolean)
}