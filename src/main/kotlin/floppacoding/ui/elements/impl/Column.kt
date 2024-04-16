package floppacoding.ui.elements.impl

import floppacoding.ui.color.IColor
import floppacoding.ui.color.alpha
import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.Type
import floppacoding.ui.constraints.measurements.Pixel
import floppacoding.ui.constraints.measurements.Undefined
import floppacoding.ui.constraints.positions.Linked
import floppacoding.ui.constraints.sizes.Bounding
import floppacoding.ui.elements.Element
import floppacoding.ui.utils.replaceUndefined

class Column(constraints: Constraints?) : Element(constraints.replaceUndefined(w = Bounding, h = Bounding)) {

    override fun draw() {
        if (color != null && color!!.rgba.alpha != 0) {
            renderer.rect(x, y, width, height, color!!.rgba)
        }
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

    fun background(color: IColor): Column {
        this.color = color
        return this
    }
}