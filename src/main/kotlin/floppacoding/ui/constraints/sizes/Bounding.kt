package floppacoding.ui.constraints.sizes

import floppacoding.ui.constraints.Constraint.Companion.HORIZONTAL
import floppacoding.ui.constraints.Size
import floppacoding.ui.constraints.Type
import floppacoding.ui.elements.Element

class Bounding : Size {

    override fun get(element: Element, type: Type): Float {
        var value = 0f
        for (child in element.elements ?: return value) {
            if (!child.enabled) continue
            val new = if (type.axis == HORIZONTAL) child.internalX + child.width else child.internalY + child.height
            if (new > value) value = new
        }
        return value
    }
}