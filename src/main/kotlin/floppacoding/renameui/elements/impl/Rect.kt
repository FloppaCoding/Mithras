package floppacoding.renameui.elements.impl

import floppacoding.renameui.constraint.Constraints
import floppacoding.renameui.elements.Element
import java.awt.Color

class Rect(constraints: Constraints, val color: Color) : Element(constraints) {
    override fun draw() {
        renderer.rect(x, y, width, height, color.rgb)
    }
}