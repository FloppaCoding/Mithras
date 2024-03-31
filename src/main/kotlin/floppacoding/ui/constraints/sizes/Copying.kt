package floppacoding.ui.constraints.sizes

import floppacoding.ui.constraints.Axis
import floppacoding.ui.constraints.Size
import floppacoding.ui.elements.Element

class Copying : Size {
    override fun get(element: Element, axis: Axis): Float {
        return when (axis) {
            Axis.HORIZONTAL -> element.parent!!.width
            Axis.VERTICAL -> element.parent!!.height
        }
    }
}