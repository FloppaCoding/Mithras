package floppacoding.ui.constraints.measurements

import floppacoding.ui.constraints.Axis
import floppacoding.ui.constraints.Measurement
import floppacoding.ui.elements.Element

class Pixel(private var pixels: Float): Measurement {
    override fun get(element: Element, axis: Axis): Float {
        return pixels
    }
}