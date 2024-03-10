package floppacoding.renameui

import floppacoding.aurora.core.Renderer2D
import floppacoding.renameui.constraint.Constraints
import floppacoding.renameui.constraint.px
import floppacoding.renameui.elements.Element
import floppacoding.renameui.elements.impl.Group
import floppacoding.renameui.events.Event
import floppacoding.renameui.events.Mouse
import java.awt.Color

// rename the stuff later
// TODO: CLEANUP CODE
class UI(val renderer: Renderer2D) {

    val main: Group = Group(Constraints(0.px, 0.px, 1920.px, 1080.px))

    private var elementHovered: Element? = null
        set(value) {
            if (field === value) return
            field?.isHovered = false
            value?.isHovered = true
            field = value
        }

    var mouseX: Float = 0f

    var mouseY: Float = 0f

    init {
        main.initialize(this)
    }

    var needsUpdate = true

    fun render() {
        renderer.beginFrame()
        renderer.scale(2f, 2f)
        main.render()
        elementHovered?.let { renderer.border(it.x, it.y, it.width, it.height, 1f, Color.WHITE.rgb) }
       // if (needsUpdate) needsUpdate = false
        renderer.endFrame()
    }

    fun onMouseClick(button: Int) {
        dispatchEvent(Mouse.Clicked(button))
    }

    fun onMouseMoved(x: Float, y: Float) {
        mouseX = x / 2f
        mouseY = y / 2f
        elementHovered = getHovered(main, x / 2f, y / 2f)
    }

    private fun getHovered(element: Element, x: Float, y: Float): Element? {
        var result: Element? = null
        if (element.enabled && element.isInside(x, y)) {
            // checks if even accepts any input
            if (element.events != null) result = element
            for (child in element.elements) {
                getHovered(child, x, y)?.let { result = it }
            }
        }
        return result
    }

/*        if (element.enabled && element.isInside(x, y)) {
            if (element.events != null) c = element
            for (i in element.elements) {
                if (c != null) {
                    c = rayCheck(c, x, y)
                }
            }
        }*/

    private fun dispatchEvent(event: Event, element: Element? = elementHovered): Boolean {
        var current = element
        while (current != null) {
            if (current.accept(event)) return true
            current = current.parent
        }
        return false
    }
}