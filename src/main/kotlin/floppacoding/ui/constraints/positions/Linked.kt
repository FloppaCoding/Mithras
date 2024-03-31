package floppacoding.ui.constraints.positions

import floppacoding.ui.constraints.Axis
import floppacoding.ui.constraints.Position
import floppacoding.ui.elements.Element

class Linked(private val link: Element?) : Position {
    override fun get(element: Element, axis: Axis): Float {
        if (link == null) return 0f
        return when (axis) {
            Axis.HORIZONTAL -> link.internalX + link.width
            Axis.VERTICAL ->link.internalY + link.height
        }
    }
}