package floppacoding.ui.constraints.positions

import floppacoding.ui.constraints.Constraint.Companion.HORIZONTAL
import floppacoding.ui.constraints.Position
import floppacoding.ui.constraints.Type
import floppacoding.ui.elements.Element

class Linked(private val link: Element?) : Position {
    override fun get(element: Element, type: Type): Float {
        if (link == null) return 0f
        return if (type.axis == HORIZONTAL) link.internalX + link.width else link.internalY + link.height
    }
}
// column position gets