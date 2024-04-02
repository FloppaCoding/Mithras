package floppacoding.ui

import floppacoding.aurora.core.Renderer2D
import floppacoding.aurora.core.TextAlign
import floppacoding.mithras.Mithras
import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.px
import floppacoding.ui.elements.Element
import floppacoding.ui.elements.impl.Group
import floppacoding.ui.events.Event
import floppacoding.ui.events.Focused
import floppacoding.ui.events.Key
import floppacoding.ui.events.Mouse
import java.util.logging.Logger

/* TODO: When finished with dsl and inputs, bring to its own window instead of inside of minecraft for benchmarking and reduce all memory usage
// TODO: Maybe split event handling into a different class
 */
class UI(val renderer: Renderer2D) {

    val main: Group = Group(Constraints(0.px, 0.px, 1920.px, 1080.px)).also { it.initialize(this) }

    private var elementHovered: Element? = null
        set(value) {
            if (field === value) return
            field?.isHovered = false
            value?.isHovered = true
            field = value
        }

    private var focused: Element? = null

    var mouseX: Float = 0f
    var mouseY: Float = 0f

    fun initialize() {
//        main.initialize(this)
    }

    // frametime metrics
    private var frames: Int = 0
    private var frameTime: Long = 0
    private var performance: String = ""

    fun render() {
        renderer.beginFrame()
        val start = System.nanoTime()
        main.render()
       // elementHovered?.let { renderer.border(it.x, it.y, it.width, it.height, 1f, java.awt.Color.WHITE.rgb) }

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
        val event = Mouse.Clicked(button)
//        if (focused != null) {
//            if (!focused!!.isInside(mouseX, mouseY)) {
//                unfocus()
//                println("Unfocused")
//            }
//            focused?.accept(event)
//            return
//        }
        dispatchEvent(event)
    }

    fun onRelease(button: Int) {
        val event = Mouse.Released(button)
//        if (focused != null) {
//            focused!!.focusedEvents?.get(event::class.java)?.let {
//                for (action in it) {
//                    action(event)
//                }
//            }
//            return
//        }
        dispatchEventGlobal(event, main)
    }


    fun onMouseMoved(x: Float, y: Float) {
        mouseX = x
        mouseY = y
        elementHovered = getHovered(x, y)
        dispatchEventGlobal(Mouse.Moved)
    }

    fun onKeyTyped(code: Int): Boolean {
        val event = Key.Typed(code)
        if (focused != null) {
            return focused!!.accept(event)
        }
        return false
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

    private fun dispatchEventGlobal(event: Event, element: Element = main) {
        element.accept(event)
        if (element.elements != null) {
            for (child in element.elements!!) {
                dispatchEventGlobal(event, child)
            }
        }
    }



    fun focus(element: Element) {
        focused?.accept(Focused.Lost)
        focused = element
        element.accept(Focused.Gained)
    }

    fun unfocus() {
        focused?.accept(Focused.Lost).also { println(it) }
        focused = null
    }

    companion object {
        val logger: Logger = Logger.getLogger("UI")
    }
}