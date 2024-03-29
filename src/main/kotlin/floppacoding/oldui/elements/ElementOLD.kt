package floppacoding.oldui.elements

import floppacoding.aurora.core.Renderer2D
import floppacoding.oldui.UI
import floppacoding.oldui.color.IColor
import floppacoding.oldui.constraint.Constraints
import floppacoding.oldui.constraint.Type
import floppacoding.oldui.events.Event
import floppacoding.oldui.events.Mouse
import org.jetbrains.annotations.MustBeInvokedByOverriders

abstract class ElementOLD(val constraints: Constraints) {

    lateinit var ui: UI

    val renderer: Renderer2D
        get() = ui.renderer

    var parent: ElementOLD? = null

    val elements: ArrayList<ElementOLD> = arrayListOf()

    var internalX = constraints.x.update(this, Type.X)
        set(value) {
            x = value + (parent?.x ?: 0f)
            field = value
        }

    var internalY = constraints.y.update(this, Type.Y)
        set(value) {
            y = value + (parent?.y ?: 0f)
            field = value
        }

    var x: Float = internalX

    var y: Float = internalY

    var width: Float = constraints.width.update(this, Type.WIDTH)

    var height: Float = constraints.height.update(this, Type.HEIGHT)

    var color: IColor? = null

    var events: HashMap<Event, ArrayList<Event.() -> Boolean>>? = null

    var isHovered = false
        set(value) {
            if (value) {
                accept(Mouse.Entered)
            } else {
                accept(Mouse.Exited)
            }
            field = value
        }

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

    fun update() {
        if (!enabled) return
        constraints.updatePosition(this)
        for (element in elements) {
            element.update()
        }
        constraints.updateSize(this)
    }

    fun render() {
        if (!enabled) return
        draw()
        renderChildren()
//        if (ui.needsUpdate) {
//            constraints.updateSize(this)
//        }
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
    open fun addElement(element: ElementOLD) {
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