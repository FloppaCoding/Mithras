package floppacoding.ui.elements.impl

import floppacoding.ui.elements.*
import org.joml.Vector4f
import java.awt.Color

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
        element.also { it ->
            if (it.constraints.x is Undefined) it.constraints.x = Pixel(0f)
            if (it.constraints.y is Undefined) {
                val last = elements?.lastOrNull { it.constraints.y is Linked }
                it.constraints.y = Linked(last)
            }
        }
    }

    override fun setupSize() {
        if (constraints.width is Undefined) constraints.width = Bounds()
        if (constraints.height is Undefined) constraints.height = Bounds()
    }
}

class Rect(constraints: Constraints?, private val radii: Vector4f?) : Element(constraints) {

    override fun draw() {
        if (radii == null) {
            renderer.rect(x, y, width, height, Color.RED.rgb)
        } else {
            renderer.roundedRect(x, y, width, height, radii, Color.RED.rgb)
        }
    }
}

class Text(val text: String, constraints: Constraints?) : Element(constraints) {

    override fun draw() {
        //renderer.border(x, y, width, height, 1f, Color.WHITE.rgb)
        renderer.text(text, x, y, Color.WHITE.rgb, 20f)
    }

    override fun setupSize() {
        constraints.width = Pixel(renderer.textWidth(text, 20f))
        constraints.height = Pixel(20f)
    }
}