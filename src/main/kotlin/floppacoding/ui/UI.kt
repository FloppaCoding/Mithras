package floppacoding.ui

import floppacoding.aurora.core.Renderer2D
import floppacoding.aurora.core.TextAlign
import floppacoding.mithras.Mithras
import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.measurements.Undefined
import floppacoding.ui.constraints.px
import floppacoding.ui.elements.Element
import floppacoding.ui.elements.impl.Group
import floppacoding.ui.events.EventManager
import java.util.logging.Logger

// TODO: When finished with dsl and inputs, bring to its own window instead of inside of minecraft for benchmarking and reduce all memory usage
class UI(val renderer: Renderer2D) {

    val main: Group = Group(Constraints(0.px, 0.px, 1920.px, 1080.px)).also { it.initialize(this) }

    var eventManager: EventManager? = EventManager(this)

    val mx get() = eventManager!!.mouseX

    val my get() = eventManager!!.mouseY

    fun initialize() {
//        main.initialize(this)
    }

    // frametime metrics
    private var frames: Int = 0
    private var frameTime: Long = 0
    private var performance: String = ""

    fun render() {
        val start = System.nanoTime()
        renderer.beginFrame()
        main.render()
        eventManager?.elementHovered?.let { renderer.border(it.x, it.y, it.width, it.height, 1f, java.awt.Color.WHITE.rgb) }

        renderer.text(performance, Mithras.mc.window.width - 2f, Mithras.mc.window.height - 2f, -1, 16f, textAlign = TextAlign.RIGHT_BOTTOM)
        renderer.endFrame()
        frames++
        frameTime += System.nanoTime() - start
        if (frames > 100) {
            performance = "undefined constraints: ${temp(main)}, frametime avg: ${(frameTime / frames) / 1_000_000.0}ms"
            frames = 0
            frameTime = 0
        }
    }

    fun temp(element: Element): Int {
        var amount = 0
        element.constraints.apply {
            if (x is Undefined) amount++
            if (y is Undefined) amount++
            if (width is Undefined) amount++
            if (height is Undefined) amount++
        }
        element.elements?.let {
            for (i in it) {
                amount += temp(i)
            }
        }
        return amount
    }

    fun resize(width: Int, height: Int) {
        main.constraints.width = width.px
        main.constraints.height = height.px
    }

    fun focus(element: Element) {
        if (eventManager == null) return logger.warning("Event Manager isn't setup, but called focus?")
        eventManager!!.focus(element)
    }

    fun unfocus() {
        if (eventManager == null) return logger.warning("Event Manager isn't setup, but called unfocus?")
        eventManager!!.unfocus()
    }

    companion object {
        val logger: Logger = Logger.getLogger("UI")
    }
}