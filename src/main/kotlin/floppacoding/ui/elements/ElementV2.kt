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

abstract class ElementV2(val constraints: Constraints) {

    lateinit var ui: UIV2

    val renderer get() = ui.renderer2D

    var parent: ElementV2? = null

    var elements: ArrayList<ElementV2>? = null


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
        internalX = constraints.x.get(this)
        internalY = constraints.y.get(this)
        width = constraints.width.get(this)
        height = constraints.height.get(this)
        draw()
        if (elements != null) {
            for (element in elements!!) {
                element.render()
            }
        }
    }

    fun addElement(elementV2: ElementV2) {
        if (elements == null) elements = arrayListOf()
        elements!!.add(elementV2)
        elementV2.parent = this
        elementV2.initialize(ui)
        setupPosition(elementV2)
    }

    fun initialize(ui: UIV2) {
        this.ui = ui
        setupSize()
    }

    // sets up position if element being added has an undefined position
    open fun setupPosition(elementV2: ElementV2) {
        elementV2.apply {
            if (constraints.x is Undefined) constraints.x = Aligning(Align.MIDDLE)
            if (constraints.y is Undefined) constraints.y = Aligning(Align.MIDDLE)
        }
    }

    open fun setupSize() {
        if (constraints.width is Undefined) constraints.x = Copying()
        if (constraints.height is Undefined) constraints.y = Copying()
    }
}

//@JvmInline
//value class Constraints private constructor(val constraints: Array<Measurement>) {
//
//    constructor(x: Measurement?, y: Measurement?, width: Measurement?, height: Measurement?) : this(arrayOf(x, y, width, height))
//
//}

class Constraints(x: Measurement, y: Measurement, width: Measurement, height: Measurement) {

    var x = x
        set(value) {
            value.axis = Axis.HORIZONTAL
            field = value
        }

    var y = y
        set(value) {
            value.axis = Axis.VERTICAL
            field = value
        }

    var width = width
        set(value) {
            value.axis = Axis.HORIZONTAL
            field = value
        }

    var height = height
        set(value) {
            value.axis = Axis.VERTICAL
            field = value
        }

    init {
        x.axis = Axis.HORIZONTAL
        y.axis = Axis.VERTICAL
        width.axis = Axis.HORIZONTAL
        height.axis = Axis.VERTICAL
    }
}

abstract class Measurement {

    var axis: Axis = Axis.VERTICAL

    abstract fun get(elementV2: ElementV2): Float
}

object Undefined : Measurement() {
    override fun get(elementV2: ElementV2): Float {
        return 0f
    }
}

abstract class Position : Measurement()

abstract class Size : Measurement()

class Pixel(private val value: Float) : Measurement() {
    override fun get(elementV2: ElementV2): Float = value
}

class Linked(private val link: ElementV2?) : Position() {
    override fun get(elementV2: ElementV2): Float {
        if (link == null) return 0f
        return when (axis) {
            Axis.HORIZONTAL -> link.internalX + link.width + 5f
            Axis.VERTICAL ->link.internalY + link.height + 5f
        }
    }
}

class Aligning(private val align: Align, val padding: Float = 0f) : Position() {
    override fun get(elementV2: ElementV2): Float {
        if (align == Align.START) return padding

        val value = if (axis == Axis.HORIZONTAL) elementV2.width else elementV2.height
        val parentValue = (if (axis == Axis.HORIZONTAL) elementV2.parent?.width else elementV2.parent?.height) ?: 0f
        return if (align == Align.MIDDLE) parentValue / 2f - value / 2f else parentValue - value - padding
    }
}


class Bounds : Size() {
    override fun get(elementV2: ElementV2): Float {
        var value = 0f
        for (child in elementV2.elements ?: return value) {
            //if (!child.enabled) continue
            when (axis) {
                Axis.HORIZONTAL -> (child.internalX + child.width).also { if (it > value) value = it }
                Axis.VERTICAL -> (child.internalY + child.height).also { if (it > value) value = it }
            }
        }
        return value
    }
}

class Copying : Size() {
    override fun get(elementV2: ElementV2): Float {
        return when (axis) {
            Axis.HORIZONTAL -> elementV2.parent!!.width
            Axis.VERTICAL -> elementV2.parent!!.height
        }
    }
}

enum class Axis {
    HORIZONTAL, VERTICAL
}

enum class Align {
    START, MIDDLE, END
}