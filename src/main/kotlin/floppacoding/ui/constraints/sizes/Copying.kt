package floppacoding.ui.constraints.sizes

import floppacoding.ui.constraints.Constraint.Companion.HORIZONTAL
import floppacoding.ui.constraints.Size
import floppacoding.ui.constraints.Type
import floppacoding.ui.elements.Element

object Copying : Size {
    override fun get(element: Element, type: Type): Float {
        return if (type.axis == HORIZONTAL) element.parent!!.width else element.parent!!.height
    }
}