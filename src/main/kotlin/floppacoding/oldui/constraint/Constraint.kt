package floppacoding.oldui.constraint

import floppacoding.oldui.elements.ElementOLD

open class Constraints(
    var x: Constraint, var y: Constraint, var width: Constraint, var height: Constraint
) {
    fun updatePosition(element: ElementOLD) {
        element.internalX = x.update(element, Type.X)
        element.internalY = y.update(element, Type.Y)
    }

    fun updateSize(element: ElementOLD) {
        element.width = width.update(element, Type.WIDTH)
        element.height = height.update(element, Type.HEIGHT)
    }
}

fun ElementOLD.getCounterpart(type: Type): Float {
    return when (type) {
        Type.X -> width
        Type.Y -> height
        Type.WIDTH -> internalX
        Type.HEIGHT -> internalY
    }
}

enum class Type {
    X, Y, WIDTH, HEIGHT
}

//interface Position {
//    fun get(element: Element): Float
//}
//
//interface Sizing {
//    fun get(element: Element): Float
//}
//
//object Placeholder2 : Position, Sizing {
//    override fun get(element: Element): Float {
//        return 0f
//    }
//}


abstract class Constraint {
    abstract fun update(element: ElementOLD, type: Type): Float
}

class Pixel(val value: Float) : Constraint() {
    override fun update(element: ElementOLD, type: Type): Float {
        return value
    }
}

class Aligning(private val align: Align, private val padding: Float = 0f) : Constraint() {

    override fun update(element: ElementOLD, type: Type): Float {
        return when (align) {
            Align.START -> padding
            Align.MIDDLE -> (element.parent?.getCounterpart(type) ?: 0f) / 2f - element.getCounterpart(type) / 2f
            Align.END -> (element.parent?.getCounterpart(type) ?: 0f) - element.getCounterpart(type) - padding
        }
    }
}

enum class Align {
    START,
    MIDDLE,
    END
}




object Placeholder : Constraint() {
    override fun update(element: ElementOLD, type: Type): Float {
        return 0f
    }
}

val Number.px get() = Pixel(this.toFloat())

fun placeholderConstraints() = Constraints(Placeholder, Placeholder, Placeholder, Placeholder)

// size based constraint
class Bounding : Constraint() {

    override fun update(element: ElementOLD, type: Type): Float {
        var value = 0f
        for (child in element.elements) {
            if (!child.enabled) continue
            when (type) {
                Type.WIDTH -> (child.x - element.x + child.width).also { if (it > value) value = it }
                Type.HEIGHT -> (child.y - element.y + child.height).also { if (it > value) value = it }
                else -> continue
            }
        }
        return value
    }
}

// todo: change directon to enum
class Linked(private val link: ElementOLD?, val padding: Float) : Constraint() {

    override fun update(element: ElementOLD, type: Type): Float {
        if (link == null) return 0f
        return when (type) {
            Type.X -> link.internalX + link.width
            Type.Y -> link.internalY + link.height
            else -> 0f
        }
    }
}

class Copy : Constraint() {

    override fun update(element: ElementOLD, type: Type): Float {
        return when (type) {
            Type.WIDTH -> element.parent?.width ?: 0f
            Type.HEIGHT -> element.parent?.height ?: 0f
            else -> 0f
        }
    }
}