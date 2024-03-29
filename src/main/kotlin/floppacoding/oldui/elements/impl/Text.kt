package floppacoding.oldui.elements.impl

import floppacoding.aurora.core.TextAlign
import floppacoding.oldui.UI
import floppacoding.oldui.color.IColor
import floppacoding.oldui.constraint.Constraints
import floppacoding.oldui.constraint.Pixel
import floppacoding.oldui.constraint.placeholderConstraints
import floppacoding.oldui.elements.ElementOLD

class Text(
    var text: String,
    constraints: Constraints? = null,
    color: IColor,
    var size: Float,
    var align: TextAlign = TextAlign.LEFT_TOP
) : ElementOLD(constraints ?: placeholderConstraints()) {

    init {
        this.color = color
    }

    override fun initialize(ui: UI) {
        super.initialize(ui)
        // todo set constraints based on textalign
        constraints.width = Pixel(renderer.textWidth(text, size))
        constraints.height = Pixel(size)
    }

    override fun draw() {
        renderer.text(text, x, y, color!!.rgba, size, textAlign = align)
    }
}