package floppacoding.ui.elements.impl

import floppacoding.ui.color.Color
import floppacoding.ui.color.alpha
import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.sizes.Copying
import floppacoding.ui.elements.Element
import floppacoding.ui.utils.replaceUndefined
import org.joml.Vector4f

open class Block(constraints: Constraints?, color: Color) : Element(constraints?.replaceUndefined(w = Copying, h = Copying)) {

    var outlineColor: Color? = null

    init {
        this.color = color
    }

    override fun draw() {
        if (color!!.rgba.alpha != 0) {
            renderer.rect(x, y, width, height, color!!.rgba)
        }
        if (outlineColor != null && outlineColor!!.rgba.alpha != 0) {
            renderer.border(x, y, width, height, 1f, null, outlineColor!!.rgba)
        }
    }

    // Maybe add width
    fun outline(color: Color): Block {
        outlineColor = color
        return this
    }
}

class RoundedBlock(constraints: Constraints?, color: Color, private val radii: Vector4f) : Block(constraints, color) {
    override fun draw() {
        if (color!!.rgba.alpha != 0) {
            renderer.roundedRect(x, y, width, height, radii, color!!.rgba)
        }
        if (outlineColor != null && outlineColor!!.rgba.alpha != 0) {
            renderer.border(x, y, width, height, 1f, radii, outlineColor!!.rgba)
        }
    }
}