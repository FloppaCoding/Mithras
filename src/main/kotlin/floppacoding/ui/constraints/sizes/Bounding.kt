package floppacoding.ui.constraints.sizes

import floppacoding.ui.constraints.Axis
import floppacoding.ui.constraints.Size
import floppacoding.ui.elements.Element

class Bounding : Size {

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