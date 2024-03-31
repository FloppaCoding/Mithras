package floppacoding.ui.elements.impl

import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.measurements.Pixel
import floppacoding.ui.constraints.measurements.Undefined
import floppacoding.ui.constraints.positions.Linked
import floppacoding.ui.constraints.sizes.Bounding
import floppacoding.ui.elements.Element
import org.joml.Vector4f
import floppacoding.ui.color.IColor as Color

class Group(constraints: Constraints?) : Element(constraints) {
    override fun draw() {
//        renderer.border(x, y, width, height, 1f, Color.WHITE.rgb)
    }
}

class Column(constraints: Constraints?) : Element(constraints) {
    override fun draw() {
        // nothing
    }

    override fun setupPosition(element: Element) {
        if (element.constraints.x is Undefined) element.constraints.x = Pixel(0f)
        if (element.constraints.y is Undefined) {
            val last = elements?.lastOrNull { it.constraints.y is Linked }
            element.constraints.y = Linked(last)
        }
    }

    override fun setupSize() {
        if (constraints.width is Undefined) constraints.width = Bounding()
        if (constraints.height is Undefined) constraints.height = Bounding()
    }
}

class Block(constraints: Constraints?, color: Color, private val radii: Vector4f?) : Element(constraints) {

    init {
        this.color = color
    }

    override fun draw() {
        if (radii == null) {
            renderer.rect(x, y, width, height, color!!.rgba)
        } else {
            renderer.roundedRect(x, y, width, height, radii, color!!.rgba)
        }
    }
}

class Text(val text: String, textColor: Color, constraints: Constraints?) : Element(constraints) {

    init {
        this.color = textColor
    }

    override fun draw() {
//        renderer.border(x, y, width, height, 1f, Color.WHITE.rgb)
        renderer.text(text, x, y, color!!.rgba, 20f)
    }

    override fun setupSize() {
        constraints.width = Pixel(renderer.textWidth(text, 20f))
        constraints.height = Pixel(20f)
    }
}