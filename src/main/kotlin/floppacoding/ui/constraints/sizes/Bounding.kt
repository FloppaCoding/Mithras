package floppacoding.ui.constraints.sizes

import floppacoding.ui.constraints.Constraint.Companion.HORIZONTAL
import floppacoding.ui.constraints.Size
import floppacoding.ui.constraints.Type
import floppacoding.ui.elements.Element
import floppacoding.ui.utils.forLoop

object Bounding : Size {

    override fun get(element: Element, type: Type): Float {
        if (element.elements == null) return 0f
        var value = 0f
        element.elements!!.forLoop {  child ->
            if (!child.enabled) return@forLoop
            val new = if (type.axis == HORIZONTAL) child.internalX + child.width else child.internalY + child.height
            if (new > value) value = new
        }
        return value
    }
}