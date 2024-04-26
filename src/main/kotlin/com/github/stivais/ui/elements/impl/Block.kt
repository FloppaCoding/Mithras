package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.alpha
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.replaceUndefined
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

class RoundedBlock(constraints: Constraints?, color: Color, radii: FloatArray) : Block(constraints, color) {

    private val radii: Vector4f

    init {
        require(radii.size == 4)
        this.radii = Vector4f(radii)
    }

    override fun draw() {
        if (color!!.rgba.alpha != 0) {
            renderer.roundedRect(x, y, width, height, radii, color!!.rgba)
        }
        if (outlineColor != null && outlineColor!!.rgba.alpha != 0) {
            renderer.border(x, y, width, height, 1f, radii, outlineColor!!.rgba)
        }
    }
}