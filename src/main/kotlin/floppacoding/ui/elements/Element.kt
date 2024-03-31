package floppacoding.ui.elements

import floppacoding.ui.UI
import floppacoding.ui.color.IColor
import floppacoding.ui.constraints.Axis
import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.measurements.Undefined
import floppacoding.ui.constraints.positions.Align
import floppacoding.ui.constraints.positions.Aligning
import floppacoding.ui.constraints.sizes.Copying
import floppacoding.ui.events.Event
import floppacoding.ui.events.Mouse


// Positioning (X or Y) should be: defined with a measurement or aligned by left/top, center, right/bottom (with some padding)
// or undefined where it gets set by the parent's element (by default best option is to center)
//
// Sizing (Width or Height) should be: defined with a measurement, copy the parents size, or wrap around elements children,
// if left undefined, it is up to the element to set what it's default value should be
//
// Required:
//  Measurements (All):
//        Pixels: Pixels on the screen
//        Animatable: Based between 2 constraints values
//        Percents: Based on parent elements position (Maybe?)
//
//  Positions (X or Y):
//        Linked: Gets position based on where the linked element ends, (Used in column)
//        Aligned: Left/Middle/Right Aligning with padding (padding doesn't apply to middle)
//
//  Sizing (Width or Height):
//        Bounding: Wraps around all children element, so they all fit
//        Copying: Copies the parent's sizing
//
// Goal: to avoid assigning positions and sizes as much as possible/mainly avoid magic numbers

abstract class Element(constraints: Constraints?) {

    val constraints: Constraints = constraints ?: Constraints(Undefined, Undefined, Undefined, Undefined)

    lateinit var ui: UI

    val renderer get() = ui.renderer

    var parent: Element? = null

    var elements: ArrayList<Element>? = null

    var events: HashMap<Event, ArrayList<Event.() -> Boolean>>? = null

    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f

    var internalX: Float = 0f
        set(value) {
            field = value
            x = value + (parent?.x ?: 0f)
        }

    var internalY: Float = 0f
        set(value) {
            field = value
            y = value + (parent?.y ?: 0f)
        }

    var color: IColor? = null

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

    var renders: Boolean = true
        get() = enabled && field

    abstract fun draw()

    fun position() {
        internalX = constraints.x.get(this, Axis.HORIZONTAL)
        internalY = constraints.y.get(this, Axis.VERTICAL)
        width = constraints.width.get(this, Axis.HORIZONTAL)
        height = constraints.height.get(this, Axis.VERTICAL)
        if (elements != null) {
            for (element in elements!!) {
                element.position()
                element.renders = element.intersects(x, y, width, height)
            }
        }
    }

    fun render() {
        if (!renders) return
        position()
        draw()
        if (elements != null) {
            for (element in elements!!) {
                element.render()
            }
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

    fun registerEvent(event: Event, block: Event.() -> Boolean) {
        if (events == null) events = HashMap()
        events!!.getOrPut(event) { arrayListOf() }.add(block)
    }

    fun addElement(element: Element) {
        if (elements == null) elements = arrayListOf()
        elements!!.add(element)
        element.parent = this
        element.initialize(ui)
        setupPosition(element)
    }

    fun initialize(ui: UI) {
        this.ui = ui
        setupSize()
    }

    // sets up position if element being added has an undefined position
    open fun setupPosition(element: Element) {
        element.apply {
            if (constraints.x is Undefined) constraints.x = Aligning(Align.MIDDLE)
            if (constraints.y is Undefined) constraints.y = Aligning(Align.MIDDLE)
        }
    }

    open fun setupSize() {
        if (constraints.width is Undefined) constraints.width = Copying()
        if (constraints.height is Undefined) constraints.height = Copying()
    }

    fun isInside(x: Float, y: Float): Boolean {
        val tx = this.x
        val ty = this.y
        return x in tx..tx + width && y in ty..ty + height
    }

    fun intersects(x: Float, y: Float, width: Float, height: Float): Boolean {
        val tx = this.x
        val ty = this.y
        val tw = this.width
        val th = this.height
        return (x <= tx + tw && tx <= x + width) && (y <= ty + th && ty <= y + height)
    }
}
