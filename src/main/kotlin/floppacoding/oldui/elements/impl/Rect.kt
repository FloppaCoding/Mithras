package floppacoding.oldui.elements.impl

import floppacoding.oldui.color.IColor
import floppacoding.oldui.constraint.Constraints
import floppacoding.oldui.constraint.placeholderConstraints
import floppacoding.oldui.elements.ElementOLD

class Rect(constraints: Constraints?, color: IColor) : ElementOLD(constraints ?: placeholderConstraints()) {

    init {
        this.color = color
    }

    override fun draw() {
        renderer.rect(x, y, width, height, color!!.rgba)
    }
}