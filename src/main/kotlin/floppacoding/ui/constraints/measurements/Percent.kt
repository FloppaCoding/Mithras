package floppacoding.ui.constraints.measurements

import floppacoding.ui.constraints.Constraint.Companion.HORIZONTAL
import floppacoding.ui.constraints.Measurement
import floppacoding.ui.constraints.Type
import floppacoding.ui.elements.Element

class Percent(private val percent: Float) : Measurement {
    override fun get(element: Element, type: Type): Float {
        return (if (type.axis == HORIZONTAL) element.parent!!.width else element.parent!!.height) * percent
    }
}