package floppacoding.renameui.constraint

import floppacoding.renameui.elements.Element

open class Constraints(
    var x: Constraint, var y: Constraint, var width: Constraint, var height: Constraint
) {
    fun update(element: Element) {
        x.value = x.update(element, 0)
        y.value = y.update(element, 1)
        width.value = width.update(element, 2)
        height.value = height.update(element, 3)
    }
}

interface Constraint {
    var value: Float

    fun update(element: Element, index: Int): Float = value
}

class Pixel(override var value: Float) : Constraint

object Placeholder : Constraint {
    override var value: Float
        get() = 0f
        set(value) { /* do nothing */ }
}

val Number.px get() = Pixel(this.toFloat())

fun placeholderConstraints() = Constraints(Placeholder, Placeholder, Placeholder, Placeholder)

// size based constraint
class Bounding : Constraint {

    override var value: Float = 0f

    override fun update(element: Element, index: Int): Float {
        var value = 0f
        for (child in element.elements) {
            when (index) {
                2 -> (child.x - element.x + child.width).also { if (it > value) value = it }
                3 -> (child.y - element.y + child.height).also { if (it > value) value = it }
            }
        }
        return value
    }
}

// todo: change directon to enum
class Linked(private val link: Element?, val padding: Float, val direction: Boolean) : Constraint {

    override var value: Float = 0f

    override fun update(element: Element, index: Int): Float {
        if (link == null) return 0f
        return when (direction) {
            false -> link.constraints.x.value + link.width
            true -> link.constraints.y.value + link.height
        }
    }
}