package floppacoding.oldui

import floppacoding.aurora.core.Renderer2D
import floppacoding.oldui.constraint.Constraints
import floppacoding.oldui.constraint.px
import floppacoding.oldui.elements.Element
import floppacoding.oldui.elements.impl.Group
import floppacoding.oldui.events.Event
import floppacoding.oldui.events.Mouse
import java.awt.Color

// rename the stuff later
// TODO: CLEANUP CODE
// TODO: Add good documentation, that is clear
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

    fun render(scale: Float) {
        renderer.beginFrame()
        renderer.scale(scale, scale) // temporary
        main.update()
        main.render()
        elementHovered?.let { renderer.border(it.x, it.y, it.width, it.height, 1f, Color.WHITE.rgb) }
       // if (needsUpdate) needsUpdate = false
        renderer.endFrame()
    }

    fun onMouseClick(button: Int) {
        dispatchEvent(Mouse.Clicked(button))
    }

    fun onRelease(button: Int) {
        dispatchEventGlobal(Mouse.Released(button), main)
    }

    // scale is a temporary fix
    fun onMouseMoved(x: Float, y: Float) {
        mouseX = x / 2f
        mouseY = y / 2f
        elementHovered = getHovered(x / 2f, y / 2f)
    }

    private fun getHovered(x: Float, y: Float, element: Element = main): Element? {
        var result: Element? = null
        if (element.enabled && element.isInside(x, y)) {
            // checks if even accepts any input/events
            if (element.events != null) result = element
            for (child in element.elements) {
                getHovered(x, y, child)?.let { result = it }
            }
        }
        return result
    }

    private fun dispatchEvent(event: Event, element: Element? = elementHovered): Boolean {
        var current = element
        while (current != null) {
            if (current.accept(event)) return true
            current = current.parent
        }
        return false
    }

    private fun dispatchEventGlobal(event: Event, element: Element) {
        element.accept(event)
        for (child in element.elements) {
            dispatchEventGlobal(event, child)
        }
    }
}