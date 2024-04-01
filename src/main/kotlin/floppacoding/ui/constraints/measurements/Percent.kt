package floppacoding.ui.constraints.measurements

import floppacoding.ui.constraints.Constraint.Companion.HORIZONTAL
import floppacoding.ui.constraints.Measurement
import floppacoding.ui.constraints.Type
import floppacoding.ui.elements.Element

class Percent(percent: Float) : Measurement {

    private var percent = percent
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    override fun get(element: Element, type: Type): Float {
        return (if (type.axis == HORIZONTAL) element.parent!!.width else element.parent!!.height) * percent
    }
}