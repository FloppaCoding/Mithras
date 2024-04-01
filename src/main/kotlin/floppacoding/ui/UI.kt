package floppacoding.ui

import floppacoding.aurora.core.Renderer2D
import floppacoding.aurora.core.TextAlign
import floppacoding.mithras.Mithras
import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.px
import floppacoding.ui.elements.Element
import floppacoding.ui.elements.impl.Group
import floppacoding.ui.events.Event
import floppacoding.ui.events.Mouse

class UI(val renderer: Renderer2D) {

    val main: Group = Group(Constraints(0.px, 0.px, 1920.px, 1080.px)).also { it.initialize(this) }

    private var elementHovered: Element? = null
        set(value) {
            if (field === value) return
            field?.isHovered = false
            value?.isHovered = true
            field = value
        }

    var mouseX: Float = 0f

    var mouseY: Float = 0f

    fun initialize() {
//        main.initialize(this)
    }

    private var frames: Int = 0
    private var frameTime: Long = 0
    private var performance: String = ""

    fun render() {
        renderer.beginFrame()
        val start = System.nanoTime()
        main.render()

        renderer.text(performance, Mithras.mc.window.width - 2f, Mithras.mc.window.height - 2f, -1, 16f, textAlign = TextAlign.RIGHT_BOTTOM)
        frames++
        frameTime += System.nanoTime() - start
        if (frames > 100) {
            performance = "frametime avg: ${(frameTime / frames) / 1_000_000.0}ms"
            frames = 0
            frameTime = 0
        }
        renderer.endFrame()
    }

    fun onMouseClick(button: Int) {
//        val start = System.nanoTime()
        dispatchEvent(Mouse.Clicked(button))
//        println(System.nanoTime() - start)
    }

    fun onRelease(button: Int) {
        dispatchEventGlobal(Mouse.Released(button), main)
    }

    fun onMouseMoved(x: Float, y: Float) {
        mouseX = x
        mouseY = y
        elementHovered = getHovered(x, y)
    }

    private fun getHovered(x: Float, y: Float, element: Element = main): Element? {
        var result: Element? = null
        if (element.renders && element.isInside(x, y)) {
            if (element.events != null) result = element // checks if even accepts any input/events
            if (element.elements != null) {
                for (child in element.elements!!) {
                    getHovered(x, y, child)?.let { result = it }
                }
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
        if (element.elements != null) {
            for (child in element.elements!!) {
                dispatchEventGlobal(event, child)
            }
        }
    }
}