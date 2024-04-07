package floppacoding.ui.elements.impl

import floppacoding.ui.color.IColor
import floppacoding.ui.constraints.Constraints
import floppacoding.ui.elements.Element
import org.joml.Vector4f

class Block(constraints: Constraints?, color: IColor, private val radii: Vector4f?) : Element(constraints) {

    var outlineColor: IColor? = null

    init {
        this.color = color
    }

    override fun draw() {
        if (radii == null) {
            renderer.rect(x, y, width, height, color!!.rgba)
        } else {
            renderer.roundedRect(x, y, width, height, radii, color!!.rgba)
        }
        if (outlineColor != null) {
            renderer.border(x, y, width, height, 1f, radii, outlineColor!!.rgba)
        }
    }

    // Maybe add width
    fun outline(color: IColor): Block {
        outlineColor = color
        return this
    }
}