package floppacoding.ui.constraints.measurements

import floppacoding.ui.constraints.Axis
import floppacoding.ui.constraints.Measurement
import floppacoding.ui.elements.Element

data object Undefined : Measurement {
    override fun get(element: Element, axis: Axis): Float {
        return 0f
    }
}