package floppacoding.ui.elements

import floppacoding.ui.UIV2


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

    lateinit var ui: UIV2

    val renderer get() = ui.renderer2D

    var parent: Element? = null

    var elements: ArrayList<Element>? = null


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

    abstract fun draw()

    fun render() {
        internalX = constraints.x.get(this, Axis.HORIZONTAL)
        internalY = constraints.y.get(this, Axis.VERTICAL)
        width = constraints.width.get(this, Axis.HORIZONTAL)
        height = constraints.height.get(this, Axis.VERTICAL)
        draw()
        if (elements != null) {
            for (element in elements!!) {
                element.render()
            }
        }
    }

    fun addElement(element: Element) {
        if (elements == null) elements = arrayListOf()
        elements!!.add(element)
        element.parent = this
        element.initialize(ui)
        setupPosition(element)
    }

    fun initialize(ui: UIV2) {
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
}

//@JvmInline
//value class Constraints private constructor(val constraints: Array<Measurement>) {
//
//    constructor(x: Measurement?, y: Measurement?, width: Measurement?, height: Measurement?) : this(arrayOf(x, y, width, height))
//
//}

class Constraints(var x: Position, var y: Position, var width: Size, var height: Size)

interface Position {
    fun get(element: Element, axis: Axis): Float
}
interface Size {
    fun get(element: Element, axis: Axis): Float
}

interface Measurement : Position, Size

data object Undefined : Measurement {
    override fun get(element: Element, axis: Axis): Float {
        return 0f
    }
}

class Pixel(private val value: Float) : Measurement {
    override fun get(element: Element, axis: Axis): Float = value
}

class Linked(private val link: Element?) : Position {
    override fun get(element: Element, axis: Axis): Float {
        if (link == null) return 0f
        return when (axis) {
            Axis.HORIZONTAL -> link.internalX + link.width
            Axis.VERTICAL ->link.internalY + link.height
        }
    }
}

class Aligning(private val align: Align, val padding: Float = 0f) : Position {
    override fun get(element: Element, axis: Axis): Float {
        if (align == Align.START) return padding

        val value = if (axis == Axis.HORIZONTAL) element.width else element.height
        val parentValue = (if (axis == Axis.HORIZONTAL) element.parent?.width else element.parent?.height) ?: 0f
        return if (align == Align.MIDDLE) parentValue / 2f - value / 2f else parentValue - value - padding
    }
}


class Bounds : Size {
    override fun get(element: Element, axis: Axis): Float {
        var value = 0f
        for (child in element.elements ?: return value) {
            //if (!child.enabled) continue
            when (axis) {
                Axis.HORIZONTAL -> (child.internalX + child.width).also { if (it > value) value = it }
                Axis.VERTICAL -> (child.internalY + child.height).also { if (it > value) value = it }
            }
        }
        return value
    }
}

class Copying : Size {
    override fun get(element: Element, axis: Axis): Float {
        return when (axis) {
            Axis.HORIZONTAL -> element.parent!!.width
            Axis.VERTICAL -> element.parent!!.height
        }
    }
}

enum class Axis {
    HORIZONTAL, VERTICAL
}

enum class Align {
    START, MIDDLE, END
}