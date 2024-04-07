package floppacoding.ui.elements.impl

import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.Type
import floppacoding.ui.constraints.measurements.Pixel
import floppacoding.ui.constraints.measurements.Undefined
import floppacoding.ui.constraints.positions.Linked
import floppacoding.ui.constraints.sizes.Bounding
import floppacoding.ui.elements.Element

class Column(constraints: Constraints?) : Element(constraints) {
    override fun draw() {
        // renderer.border(x, y, width, height, 1f, java.awt.Color.WHITE.rgb)
    }

    override fun setupPosition(element: Element) {
        if (element.constraints.x is Undefined) element.constraints.x = Pixel(0f)
        if (element.constraints.y is Undefined) {
            val last = elements?.lastOrNull { it.constraints.y is Linked }
            element.constraints.y = Linked(last)
            element.y = element.constraints.y.get(element, Type.Y)
            height = constraints.height.get(element, Type.H)
        }
    }

    override fun setupSize() {
        if (constraints.width is Undefined) constraints.width = Bounding()
        if (constraints.height is Undefined) constraints.height = Bounding()
    }
}