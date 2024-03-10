package floppacoding.renameui.elements

import floppacoding.aurora.core.Renderer2D
import floppacoding.renameui.UI
import floppacoding.renameui.constraint.Constraints
import floppacoding.renameui.events.Event
import org.jetbrains.annotations.MustBeInvokedByOverriders

abstract class Element(val constraints: Constraints) {

    lateinit var ui: UI

    val renderer: Renderer2D
        get() = ui.renderer

    var parent: Element? = null

    val elements: ArrayList<Element> = arrayListOf()

    var x: Float = constraints.x.value

    var y: Float = constraints.y.value

    val width: Float
        get() = constraints.width.value

    val height: Float
        get() = constraints.height.value

    var events: HashMap<Event, ArrayList<Event.() -> Boolean>>? = null

    var isHovered = false

    var enabled: Boolean = true

/*

    @ApiStatus.Internal
    var renderX = constraints.x.value
        set(value) {
            field = value + (parent?.renderX ?: 0f)
        }

    @ApiStatus.Internal
    var renderY = constraints.y.value
        set(value) {
            field = value + (parent?.renderY ?: 0f)
        }
*/

    abstract fun draw()


    fun render() {
        if (!enabled) return
        if (ui.needsUpdate) {
            constraints.update(this)
            x = constraints.x.value + (parent?.x ?: 0f)
            y = constraints.y.value + (parent?.y ?: 0f)
        }
        draw()
        renderChildren()
    }

    open fun renderChildren() {
        for (element in elements) {
            element.render()
        }
    }

    open fun initialize(ui: UI) {
        this.ui = ui
        for (element in elements) {
            element.initialize(ui)
        }
    }

    fun accept(event: Event): Boolean {
        if (events != null) {
            events?.get(event)?.let {
                for (block in it) {
                    if (block(event)) return true
                }
            }
        }
        return false
    }

    @MustBeInvokedByOverriders
    open fun addElement(element: Element) {
        if (::ui.isInitialized) element.initialize(ui)
        element.parent = this
        elements.add(element)
        if (::ui.isInitialized) ui.needsUpdate = true
    }

    fun registerEvent(event: Event, block: Event.() -> Boolean) {
        if (events == null) events = HashMap()
        events!!.getOrPut(event) { arrayListOf() }.add(block)
    }

    fun isInside(x: Float, y: Float): Boolean {
        val tx = this.x
        val ty = this.y
        return x in tx..tx + width && y in ty..ty + height
    }
}